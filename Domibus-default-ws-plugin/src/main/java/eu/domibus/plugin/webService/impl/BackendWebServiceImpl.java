/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin.webService.impl;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartProperties;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.webService.generated.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("ValidExternallyBoundObject")
@javax.jws.WebService(
        serviceName = "BackendService_1_1",
        portName = "BACKEND_PORT",
        targetNamespace = "http://org.ecodex.backend/1_1/",
        endpointInterface = "eu.domibus.plugin.webService.generated.BackendInterface")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class BackendWebServiceImpl extends AbstractBackendConnector<Messaging, UserMessage> implements BackendInterface {

    private static final Log LOG = LogFactory.getLog(BackendWebServiceImpl.class);
    private static final eu.domibus.plugin.webService.generated.ObjectFactory WEBSERVICE_OF = new eu.domibus.plugin.webService.generated.ObjectFactory();
    private static final eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory EBMS_OBJECT_FACTORY = new eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory();

    @Autowired
    private MessageRetrievalTransformer<UserMessage> messageRetrievalTransformer;
    @Autowired
    private MessageSubmissionTransformer<Messaging> messageSubmissionTransformer;

    public BackendWebServiceImpl(final String name) {
        super(name);

    }

    @SuppressWarnings("ValidExternallyBoundObject")
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SendResponse sendMessage(final SendRequest sendRequest, final Messaging ebMSHeaderInfo) throws SendMessageFault {

        BackendWebServiceImpl.LOG.debug("Transforming incoming message");

        final PayloadType bodyload = sendRequest.getBodyload();

        List<eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo> partInfoList = ebMSHeaderInfo.getUserMessage().getPayloadInfo().getPartInfo();

        for (Iterator<eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo> i = partInfoList.iterator(); i.hasNext(); ) {

            PartInfo extendedPartInfo = new PartInfo(i.next());
            i.remove();

            boolean foundPayload = false;
            final String href = extendedPartInfo.getHref();
            BackendWebServiceImpl.LOG.debug("Looking for payload: " + href);
            for (final PayloadType payload : sendRequest.getPayload()) {
                BackendWebServiceImpl.LOG.debug("comparing with payload id: " + payload.getPayloadId());
                if (payload.getPayloadId().equals(href)) {

                    this.copyPartProperties(payload, extendedPartInfo);
                    extendedPartInfo.setInBody(false);
                    extendedPartInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(payload.getValue(), null)));
                    foundPayload = true;
                    break;
                }
            }
            if (!foundPayload) {
                if (bodyload == null) {
                    // in this case the payload referenced in the partInfo was neither an external payload nor a bodyload
                    FaultDetail detail = new FaultDetail();
                    detail.setCode("TODO");
                    detail.setMessage(extendedPartInfo.getHref());
                    throw new SendMessageFault("No Payload or Bodyload found for PartInfo with href: ", detail);
                }

                //can only be in bodyload, href MAY be null!
                if (href == null && bodyload.getPayloadId() == null ||
                        href != null && href.equals(bodyload.getPayloadId())) {

                    this.copyPartProperties(bodyload, extendedPartInfo);
                    extendedPartInfo.setInBody(true);
                    extendedPartInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(bodyload.getValue(), "text/xml")));
                } else {
                    FaultDetail detail = new FaultDetail();
                    detail.setCode("TODO");
                    detail.setMessage(extendedPartInfo.getHref());
                    throw new SendMessageFault("No payload found for PartInfo with href: ", detail);
                }
            }


        }

        ebMSHeaderInfo.getUserMessage().getMessageInfo().setTimestamp(new XMLGregorianCalendarImpl());
        final String messageId;
        try {
            messageId = this.submit(ebMSHeaderInfo);
        } catch (final MessagingProcessingException mpEx) {
            BackendWebServiceImpl.LOG.error("Message submission failed", mpEx);
            throw new SendMessageFault("Message submission failed", generateFaultDetail(mpEx));
        }
        BackendWebServiceImpl.LOG.debug("Received message from backend to send, assigning messageID" + messageId);
        final SendResponse response = BackendWebServiceImpl.WEBSERVICE_OF.createSendResponse();
        response.getMessageID().add(messageId);
        return response;
    }

    private FaultDetail generateFaultDetail(MessagingProcessingException mpEx) {
        FaultDetail fd = WEBSERVICE_OF.createFaultDetail();
        fd.setCode(mpEx.getEbms3ErrorCode().getErrorCodeName());
        fd.setMessage(mpEx.getMessage());
        return fd;
    }


    private void copyPartProperties(final PayloadType payload, final PartInfo partInfo) {
        final PartProperties partProperties = new PartProperties();
        Property prop;

        // add all partproperties WEBSERVICE_OF the backend message
        if (partInfo.getPartProperties() != null) {
            for (final Property property : partInfo.getPartProperties().getProperties()) {
                prop = new Property();

                prop.setName(property.getName());
                prop.setValue(property.getValue());
                partProperties.getProperties().add(prop);
            }
        }

        boolean mimeTypePropFound = false;
        for (final Property property : partProperties.getProperties()) {
            if (Property.MIME_TYPE.equals(property.getName())) {
                mimeTypePropFound = true;
                break;
            }
        }
        // in case there was no property with name {@value Property.MIME_TYPE} and xmime:contentType attribute was set noinspection SuspiciousMethodCalls
        if (!mimeTypePropFound && payload.getContentType() != null) {
            prop = new Property();
            prop.setName(Property.MIME_TYPE);
            prop.setValue(payload.getContentType());
            partProperties.getProperties().add(prop);
        }

        partInfo.setPartProperties(partProperties);
    }


    @Override
    public ListPendingMessagesResponse listPendingMessages(final Object listPendingMessagesRequest) {
        final ListPendingMessagesResponse response = BackendWebServiceImpl.WEBSERVICE_OF.createListPendingMessagesResponse();
        final Collection<String> pending = this.listPendingMessages();
        response.getMessageID().addAll(pending);
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = DownloadMessageFault.class)
    public void downloadMessage(final DownloadMessageRequest downloadMessageRequest, final Holder<DownloadMessageResponse> downloadMessageResponse, final Holder<Messaging> ebMSHeaderInfo) throws DownloadMessageFault {
        UserMessage userMessage = null;
        try {
            if (downloadMessageRequest.getMessageID() != null && !downloadMessageRequest.getMessageID().isEmpty()) {
                userMessage = this.downloadMessage(downloadMessageRequest.getMessageID(), null);
            }
        } catch (final MessageNotFoundException e) {
            BackendWebServiceImpl.LOG.error("Downloading message failed", e);
            throw new DownloadMessageFault("Downloading message failed, reason: " + e.getMessage());
        }
        final Messaging result = BackendWebServiceImpl.EBMS_OBJECT_FACTORY.createMessaging();
        result.setUserMessage(userMessage);
        ebMSHeaderInfo.value = result;
        downloadMessageResponse.value = BackendWebServiceImpl.WEBSERVICE_OF.createDownloadMessageResponse();

        for (final PartInfo partInfo : result.getUserMessage().getPayloadInfo().getPartInfo()) {
            final PayloadType payloadType = BackendWebServiceImpl.WEBSERVICE_OF.createPayloadType();
            try {
                payloadType.setValue(IOUtils.toByteArray(partInfo.getPayloadDatahandler().getInputStream()));
            } catch (final IOException e) {
                LOG.error("", e);
                throw new DownloadMessageFault(e.getMessage());
            }
            if (partInfo.isInBody()) {
                partInfo.setHref("#bodyload");
                payloadType.setPayloadId("#bodyload");
                downloadMessageResponse.value.setBodyload(payloadType);
                continue;
            }
            payloadType.setPayloadId(partInfo.getHref());
            downloadMessageResponse.value.getPayload().add(payloadType);
        }
    }

    @Override
    public MessageStatus getMessageStatus(final MessageStatusRequest messageStatusRequest) {
        return this.messageRetriever.getMessageStatus(messageStatusRequest.getMessageID());
    }

    @Override
    public ErrorResultImplArray getMessageErrors(final GetErrorsRequest messageErrorsRequest) {
        List<? extends ErrorResult> res = this.messageRetriever.getErrorsForMessage(messageErrorsRequest.getMessageID());
        ErrorResultImplArray result = new ErrorResultImplArray();
        for (ErrorResult errorResult : res) {
            ErrorResultImpl errorResultImpl = new ErrorResultImpl();
            result.getItem().add(new ErrorResultImpl(errorResult));
        }
        return result;

    }

    @Override
    public MessageSubmissionTransformer<Messaging> getMessageSubmissionTransformer() {
        return this.messageSubmissionTransformer;
    }

    @Override
    public MessageRetrievalTransformer<UserMessage> getMessageRetrievalTransformer() {
        return this.messageRetrievalTransformer;
    }

    @Override
    public void messageReceiveFailed(final String messageId, final String ednpoint) {
        throw new UnsupportedOperationException("Operation not yet implemented");
    }

    @Override
    public void messageSendFailed(final String messageId) {
        throw new UnsupportedOperationException("Operation not yet implemented");
    }

}
