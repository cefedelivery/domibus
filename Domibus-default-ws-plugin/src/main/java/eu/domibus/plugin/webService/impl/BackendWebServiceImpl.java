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
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.DomibusServiceException;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.webService.generated.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;


@SuppressWarnings("ValidExternallyBoundObject")
@javax.jws.WebService(
        serviceName = "BackendService_1_1",
        portName = "BACKEND_PORT",
        targetNamespace = "http://org.ecodex.backend/1_1/",
        endpointInterface = "eu.domibus.plugin.webService.generated.BackendInterface")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class BackendWebServiceImpl extends AbstractBackendConnector<Messaging, UserMessage> implements BackendInterface {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendWebServiceImpl.class);

    private static final eu.domibus.plugin.webService.generated.ObjectFactory WEBSERVICE_OF = new eu.domibus.plugin.webService.generated.ObjectFactory();

    private static final ObjectFactory EBMS_OBJECT_FACTORY = new ObjectFactory();

    private static final String MIME_TYPE = "MimeType";

    private static final String DEFAULT_MT = "text/xml";

    private static final String BODYLOAD = "#bodyload";

    private static final String MESSAGE_NOT_FOUND_ID = "Message not found, id [";

    private static final String ERROR_IS_PAYLOAD_DATA_HANDLER = "Error getting the input stream from the payload data handler";

    @Autowired
    private StubDtoTransformer defaultTransformer;

    @Autowired
    private MessageAcknowledgeService messageAcknowledgeService;

    public BackendWebServiceImpl(final String name) {
        super(name);
    }

    /**
     * @param sendRequest
     * @param ebMSHeaderInfo
     * @return
     * @throws SendMessageFault
     * @deprecated Use sendMessageWithLargeFilesSupport
     */
    @Deprecated
    @SuppressWarnings("ValidExternallyBoundObject")
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SendResponse sendMessage(final SendRequest sendRequest, final Messaging ebMSHeaderInfo) throws SendMessageFault {
        LOG.info("Received message");

        final PayloadType bodyload = sendRequest.getBodyload();

        List<PartInfo> partInfoList = ebMSHeaderInfo.getUserMessage().getPayloadInfo().getPartInfo();

        List<ExtendedPartInfo> partInfosToAdd = new ArrayList<>();

        for (Iterator<PartInfo> i = partInfoList.iterator(); i.hasNext(); ) {

            ExtendedPartInfo extendedPartInfo = new ExtendedPartInfo(i.next());
            partInfosToAdd.add(extendedPartInfo);
            i.remove();

            boolean foundPayload = false;
            final String href = extendedPartInfo.getHref();
            LOG.debug("Looking for payload: " + href);
            for (final PayloadType payload : sendRequest.getPayload()) {
                LOG.debug("comparing with payload id: " + payload.getPayloadId());
                if (StringUtils.equalsIgnoreCase(payload.getPayloadId(), href)) {
                    this.copyPartProperties(payload.getContentType(), extendedPartInfo);
                    extendedPartInfo.setInBody(false);
                    LOG.debug("sendMessage - payload Content Type: " + payload.getContentType());
                    extendedPartInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(payload.getValue(), payload.getContentType() == null ? DEFAULT_MT : payload.getContentType())));
                    foundPayload = true;
                    break;
                }
            }
            if (!foundPayload) {
                if (bodyload == null) {
                    // in this case the payload referenced in the partInfo was neither an external payload nor a bodyload
                    throw new SendMessageFault("No Payload or Bodyload found for PartInfo with href: " + extendedPartInfo.getHref(), generateDefaultFaultDetail(extendedPartInfo.getHref()));
                }
                // It can only be in body load, href MAY be null!
                if (href == null && bodyload.getPayloadId() == null || href != null && StringUtils.equalsIgnoreCase(href, bodyload.getPayloadId())) {
                    this.copyPartProperties(bodyload.getContentType(), extendedPartInfo);
                    extendedPartInfo.setInBody(true);
                    LOG.debug("sendMessage - bodyload Content Type: " + bodyload.getContentType());
                    extendedPartInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(bodyload.getValue(), bodyload.getContentType() == null ? DEFAULT_MT : bodyload.getContentType())));
                } else {
                    throw new SendMessageFault("No payload found for PartInfo with href: " + extendedPartInfo.getHref(), generateDefaultFaultDetail(extendedPartInfo.getHref()));
                }
            }
        }
        partInfoList.addAll(partInfosToAdd);
        if (ebMSHeaderInfo.getUserMessage().getMessageInfo() == null) {
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setTimestamp(getXMLTimeStamp());
            ebMSHeaderInfo.getUserMessage().setMessageInfo(messageInfo);
        }
        final String messageId;
        try {
            messageId = this.submit(ebMSHeaderInfo);
        } catch (final MessagingProcessingException mpEx) {
            LOG.error("Message submission failed", mpEx);
            throw new SendMessageFault("Message submission failed", generateFaultDetail(mpEx));
        }
        LOG.info("Received message from backend to send, assigning messageID" + messageId);
        final SendResponse response = WEBSERVICE_OF.createSendResponse();
        response.getMessageID().add(messageId);
        return response;
    }


    /**
     * Add support for large files using DataHandler instead of byte[]
     *
     * @param submitRequest
     * @param ebMSHeaderInfo
     * @return
     * @throws SendMessageFault
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    public SubmitResponse submitMessage(SubmitRequest submitRequest, Messaging ebMSHeaderInfo) throws SendMessageFault {
        LOG.info("Received message");

        final LargePayloadType bodyload = submitRequest.getBodyload();

        List<PartInfo> partInfoList = ebMSHeaderInfo.getUserMessage().getPayloadInfo().getPartInfo();

        List<ExtendedPartInfo> partInfosToAdd = new ArrayList<>();

        for (Iterator<PartInfo> i = partInfoList.iterator(); i.hasNext(); ) {

            ExtendedPartInfo extendedPartInfo = new ExtendedPartInfo(i.next());
            partInfosToAdd.add(extendedPartInfo);
            i.remove();

            boolean foundPayload = false;
            final String href = extendedPartInfo.getHref();
            LOG.debug("Looking for payload: " + href);
            for (final LargePayloadType payload : submitRequest.getPayload()) {
                LOG.debug("comparing with payload id: " + payload.getPayloadId());
                if (StringUtils.equalsIgnoreCase(payload.getPayloadId(), href)) {
                    this.copyPartProperties(payload.getContentType(), extendedPartInfo);
                    extendedPartInfo.setInBody(false);
                    LOG.debug("sendMessage - payload Content Type: " + payload.getContentType());
                    extendedPartInfo.setPayloadDatahandler(payload.getValue());
                    foundPayload = true;
                    break;
                }
            }
            if (!foundPayload) {
                if (bodyload == null) {
                    // in this case the payload referenced in the partInfo was neither an external payload nor a bodyload
                    throw new SendMessageFault("No Payload or Bodyload found for PartInfo with href: " + extendedPartInfo.getHref(), generateDefaultFaultDetail(extendedPartInfo.getHref()));
                }
                // It can only be in body load, href MAY be null!
                if (href == null && bodyload.getPayloadId() == null || href != null && StringUtils.equalsIgnoreCase(href, bodyload.getPayloadId())) {
                    this.copyPartProperties(bodyload.getContentType(), extendedPartInfo);
                    extendedPartInfo.setInBody(true);
                    LOG.debug("sendMessage - bodyload Content Type: " + bodyload.getContentType());
                    extendedPartInfo.setPayloadDatahandler(bodyload.getValue());
                } else {
                    throw new SendMessageFault("No payload found for PartInfo with href: " + extendedPartInfo.getHref(), generateDefaultFaultDetail(extendedPartInfo.getHref()));
                }
            }
        }
        partInfoList.addAll(partInfosToAdd);
        if (ebMSHeaderInfo.getUserMessage().getMessageInfo() == null) {
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setTimestamp(getXMLTimeStamp());
            ebMSHeaderInfo.getUserMessage().setMessageInfo(messageInfo);
        }
        final String messageId;
        try {
            messageId = this.submit(ebMSHeaderInfo);
        } catch (final MessagingProcessingException mpEx) {
            LOG.error("Message submission failed", mpEx);
            throw new SendMessageFault("Message submission failed", generateFaultDetail(mpEx));
        }
        LOG.info("Received message from backend to send, assigning messageID" + messageId);
        final SubmitResponse response = WEBSERVICE_OF.createSubmitResponse();
        response.getMessageID().add(messageId);
        return response;
    }

    protected XMLGregorianCalendar getXMLTimeStamp() {
        GregorianCalendar gc = new GregorianCalendar();
        return new XMLGregorianCalendarImpl(gc);
    }

    private FaultDetail generateFaultDetail(MessagingProcessingException mpEx) {
        FaultDetail fd = WEBSERVICE_OF.createFaultDetail();
        fd.setCode(mpEx.getEbms3ErrorCode().getErrorCodeName());
        fd.setMessage(mpEx.getMessage());
        return fd;
    }

    private FaultDetail generateDefaultFaultDetail(String message) {
        FaultDetail fd = WEBSERVICE_OF.createFaultDetail();
        fd.setCode(ErrorCode.EBMS_0004.name());
        fd.setMessage(message);
        return fd;
    }

    private void copyPartProperties(final String payloadContentType, final ExtendedPartInfo partInfo) {
        final PartProperties partProperties = new PartProperties();
        Property prop;

        // add all partproperties WEBSERVICE_OF the backend message
        if (partInfo.getPartProperties() != null) {
            for (final Property property : partInfo.getPartProperties().getProperty()) {
                prop = new Property();

                prop.setName(property.getName());
                prop.setValue(property.getValue());
                partProperties.getProperty().add(prop);
            }
        }

        boolean mimeTypePropFound = false;
        for (final Property property : partProperties.getProperty()) {
            if (MIME_TYPE.equals(property.getName())) {
                mimeTypePropFound = true;
                break;
            }
        }
        // in case there was no property with name {@value Property.MIME_TYPE} and xmime:contentType attribute was set noinspection SuspiciousMethodCalls
        if (!mimeTypePropFound && payloadContentType != null) {
            prop = new Property();
            prop.setName(MIME_TYPE);
            prop.setValue(payloadContentType);
            partProperties.getProperty().add(prop);
        }
        partInfo.setPartProperties(partProperties);
    }

    @Override
    public ListPendingMessagesResponse listPendingMessages(final Object listPendingMessagesRequest) {
        final ListPendingMessagesResponse response = WEBSERVICE_OF.createListPendingMessagesResponse();
        final Collection<String> pending = this.listPendingMessages();
        response.getMessageID().addAll(pending);
        return response;
    }

    /**
     * @param downloadMessageRequest
     * @param downloadMessageResponse
     * @param ebMSHeaderInfo
     * @throws DownloadMessageFault
     * @deprecated Use downloadMessageWithLargeFilesSupport
     */
    @Deprecated
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = DownloadMessageFault.class)
    public void downloadMessage(final DownloadMessageRequest downloadMessageRequest, Holder<DownloadMessageResponse> downloadMessageResponse, Holder<Messaging> ebMSHeaderInfo) throws DownloadMessageFault {

        UserMessage userMessage = null;
        boolean isMessageIdNotEmpty = StringUtils.isNotEmpty(downloadMessageRequest.getMessageID());

        try {
            if (isMessageIdNotEmpty) {
                userMessage = downloadMessage(downloadMessageRequest.getMessageID(), null);
            }
        } catch (final MessageNotFoundException mnfEx) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]", mnfEx);
            }
            LOG.error(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]");
            throw new DownloadMessageFault(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]", createDownloadMessageFault(mnfEx));
        }

        if (userMessage == null) {
            LOG.error(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]");
            throw new DownloadMessageFault(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]", createFault("UserMessage not found"));
        }

        // To avoid blocking errors during the Header's response validation
        if (StringUtils.isEmpty(userMessage.getCollaborationInfo().getAgreementRef().getValue())) {
            userMessage.getCollaborationInfo().setAgreementRef(null);
        }
        Messaging messaging = EBMS_OBJECT_FACTORY.createMessaging();
        messaging.setUserMessage(userMessage);
        ebMSHeaderInfo.value = messaging;
        downloadMessageResponse.value = WEBSERVICE_OF.createDownloadMessageResponse();

        fillInfoParts(downloadMessageResponse, messaging);

        try {
            messageAcknowledgeService.acknowledgeMessageDelivered(downloadMessageRequest.getMessageID(), new Timestamp(System.currentTimeMillis()));
        } catch (AuthenticationException | MessageAcknowledgeException e) {
            //if an error occurs related to the message acknowledgement do not block the download message operation
            LOG.error("Error acknowledging message [" + downloadMessageRequest.getMessageID() + "]", e);
        }
    }

    protected DownloadMessageFault createFault(String message, DomibusServiceException e) {
        FaultDetail detail = WEBSERVICE_OF.createFaultDetail();
        detail.setCode(e.getErrorCode().getErrorCode());
        detail.setMessage(e.getMessage());
        return new DownloadMessageFault(message, detail);
    }

    /**
     * Add support for large files using DataHandler instead of byte[]
     *
     * @param retrieveMessageRequest
     * @param retrieveMessageResponse
     * @param ebMSHeaderInfo
     * @throws DownloadMessageFault
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = DownloadMessageFault.class)
    public void retrieveMessage(RetrieveMessageRequest retrieveMessageRequest, Holder<RetrieveMessageResponse> retrieveMessageResponse, Holder<Messaging> ebMSHeaderInfo) throws DownloadMessageFault {

        UserMessage userMessage = null;
        boolean isMessageIdNotEmpty = StringUtils.isNotEmpty(retrieveMessageRequest.getMessageID());

        try {
            if (isMessageIdNotEmpty) {
                userMessage = downloadMessage(retrieveMessageRequest.getMessageID(), null);
            }
        } catch (final MessageNotFoundException mnfEx) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]", mnfEx);
            }
            LOG.error(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]");
            throw new DownloadMessageFault(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]", createDownloadMessageFault(mnfEx));
        }

        if (userMessage == null) {
            LOG.error(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]");
            throw new DownloadMessageFault(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]", createFault("UserMessage not found"));
        }

        // To avoid blocking errors during the Header's response validation
        if (StringUtils.isEmpty(userMessage.getCollaborationInfo().getAgreementRef().getValue())) {
            userMessage.getCollaborationInfo().setAgreementRef(null);
        }
        Messaging messaging = EBMS_OBJECT_FACTORY.createMessaging();
        messaging.setUserMessage(userMessage);
        ebMSHeaderInfo.value = messaging;
        retrieveMessageResponse.value = WEBSERVICE_OF.createRetrieveMessageResponse();

        fillInfoPartsForLargeFiles(retrieveMessageResponse, messaging);

        try {
            messageAcknowledgeService.acknowledgeMessageDelivered(retrieveMessageRequest.getMessageID(), new Timestamp(System.currentTimeMillis()));
        } catch (AuthenticationException | MessageAcknowledgeException e) {
            //if an error occurs related to the message acknowledgement do not block the download message operation
            LOG.error("Error acknowledging message [" + retrieveMessageRequest.getMessageID() + "]", e);
        }
    }


    private void fillInfoParts(Holder<DownloadMessageResponse> downloadMessageResponse, Messaging messaging) throws DownloadMessageFault {

        String msgId = messaging.getUserMessage().getMessageInfo().getMessageId();

        for (final PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            ExtendedPartInfo extPartInfo = (ExtendedPartInfo) partInfo;
            PayloadType payloadType = WEBSERVICE_OF.createPayloadType();
            try {
                if(extPartInfo.getPayloadDatahandler() != null ) {
                    payloadType.setValue(IOUtils.toByteArray(extPartInfo.getPayloadDatahandler().getInputStream()));
                    LOG.debug("downloadMessage - payloadDatahandler Content Type: " + extPartInfo.getPayloadDatahandler().getContentType());
                }
            } catch (final IOException ioEx) {
                LOG.error(ERROR_IS_PAYLOAD_DATA_HANDLER, ioEx);
                throw new DownloadMessageFault(ERROR_IS_PAYLOAD_DATA_HANDLER, createDownloadMessageFault(ioEx));
            }
            if (extPartInfo.isInBody()) {
                extPartInfo.setHref(BODYLOAD);
                payloadType.setPayloadId(BODYLOAD);
                downloadMessageResponse.value.setBodyload(payloadType);
            } else {
                payloadType.setPayloadId(partInfo.getHref());
                downloadMessageResponse.value.getPayload().add(payloadType);
            }
        }
    }

    private void fillInfoPartsForLargeFiles(Holder<RetrieveMessageResponse> retrieveMessageResponse, Messaging messaging) throws DownloadMessageFault {
        for (final PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            ExtendedPartInfo extPartInfo = (ExtendedPartInfo) partInfo;
            LargePayloadType payloadType = WEBSERVICE_OF.createLargePayloadType();
            LOG.debug("payloadDatahandler Content Type: " + extPartInfo.getPayloadDatahandler().getContentType());
            payloadType.setValue(extPartInfo.getPayloadDatahandler());
            if (extPartInfo.isInBody()) {
                extPartInfo.setHref(BODYLOAD);
                payloadType.setPayloadId(BODYLOAD);
                retrieveMessageResponse.value.setBodyload(payloadType);
            } else {
                payloadType.setPayloadId(partInfo.getHref());
                retrieveMessageResponse.value.getPayload().add(payloadType);
            }
        }
    }

    private FaultDetail createDownloadMessageFault(Exception ex) {
        FaultDetail detail = WEBSERVICE_OF.createFaultDetail();
        detail.setCode(eu.domibus.common.ErrorCode.EBMS_0004.getErrorCodeName());
        if (ex instanceof MessagingProcessingException) {
            MessagingProcessingException mpEx = (MessagingProcessingException) ex;
            detail.setCode(mpEx.getEbms3ErrorCode().getErrorCodeName());
            detail.setMessage(mpEx.getMessage());
        } else {
            detail.setMessage(ex.getMessage());
        }
        return detail;
    }

    private FaultDetail createFault(String message) {
        FaultDetail detail = WEBSERVICE_OF.createFaultDetail();
        detail.setCode(eu.domibus.common.ErrorCode.EBMS_0004.getErrorCodeName());
        detail.setMessage(message);
        return detail;
    }


    @Override
    public MessageStatus getMessageStatus(final GetStatusRequest messageStatusRequest) {
        return defaultTransformer.transformFromMessageStatus(messageRetriever.getMessageStatus(messageStatusRequest.getMessageID()));
    }

    @Override
    public ErrorResultImplArray getMessageErrors(final GetErrorsRequest messageErrorsRequest) {
        return defaultTransformer.transformFromErrorResults(messageRetriever.getErrorsForMessage(messageErrorsRequest.getMessageID()));
    }

    @Override
    public MessageSubmissionTransformer<Messaging> getMessageSubmissionTransformer() {
        return this.defaultTransformer;
    }

    @Override
    public MessageRetrievalTransformer<UserMessage> getMessageRetrievalTransformer() {
        return this.defaultTransformer;
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
