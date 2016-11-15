package eu.domibus.plugin.webService.impl;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.webService.generated.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.util.*;


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

    private static final ObjectFactory EBMS_OBJECT_FACTORY = new ObjectFactory();

    private static final String MIME_TYPE = "MimeType";

    private static final String DEFAULT_MT = "text/xml";

    private static final String MESSAGE_NOT_FOUND_ID = "Message not found, id [";

    private static final String ERROR_IS_PAYLOAD_DATA_HANDLER = "Error getting the input stream from the payload data handler";

    @Autowired
    private StubDtoTransformer defaultTransformer;

    public BackendWebServiceImpl(final String name) {
        super(name);
    }

    @SuppressWarnings("ValidExternallyBoundObject")
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SendResponse sendMessage(final SendRequest sendRequest, final Messaging ebMSHeaderInfo) throws SendMessageFault {

        BackendWebServiceImpl.LOG.debug("Transforming incoming message");

        final PayloadType bodyload = sendRequest.getBodyload();

        List<PartInfo> partInfoList = ebMSHeaderInfo.getUserMessage().getPayloadInfo().getPartInfo();

        List<ExtendedPartInfo> partInfosToAdd = new ArrayList<>();

        for (Iterator<PartInfo> i = partInfoList.iterator(); i.hasNext(); ) {

            ExtendedPartInfo extendedPartInfo = new ExtendedPartInfo(i.next());
            partInfosToAdd.add(extendedPartInfo);
            i.remove();

            boolean foundPayload = false;
            final String href = extendedPartInfo.getHref();
            BackendWebServiceImpl.LOG.debug("Looking for payload: " + href);
            for (final PayloadType payload : sendRequest.getPayload()) {
                BackendWebServiceImpl.LOG.debug("comparing with payload id: " + payload.getPayloadId());
                if (payload.getPayloadId().equals(href)) {
                    this.copyPartProperties(payload, extendedPartInfo);
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
                if (href == null && bodyload.getPayloadId() == null || href != null && href.equals(bodyload.getPayloadId())) {
                    this.copyPartProperties(bodyload, extendedPartInfo);
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
            BackendWebServiceImpl.LOG.error("Message submission failed", mpEx);
            throw new SendMessageFault("Message submission failed", generateFaultDetail(mpEx));
        }
        BackendWebServiceImpl.LOG.debug("Received message from backend to send, assigning messageID" + messageId);
        final SendResponse response = BackendWebServiceImpl.WEBSERVICE_OF.createSendResponse();
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

    private void copyPartProperties(final PayloadType payload, final ExtendedPartInfo partInfo) {
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
        if (!mimeTypePropFound && payload.getContentType() != null) {
            prop = new Property();
            prop.setName(MIME_TYPE);
            prop.setValue(payload.getContentType());
            partProperties.getProperty().add(prop);
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
        boolean isMessageIdValued = StringUtils.isNotEmpty(downloadMessageRequest.getMessageID());

        try {
            if (isMessageIdValued) {
                userMessage = downloadMessage(downloadMessageRequest.getMessageID(), null);
            }
        } catch (final MessageNotFoundException mnfEx) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]", mnfEx);
            }
            LOG.error(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]");
            throw new DownloadMessageFault(MESSAGE_NOT_FOUND_ID + downloadMessageRequest.getMessageID() + "]", createDownloadMessageFault(mnfEx));
        }

        Messaging result = BackendWebServiceImpl.EBMS_OBJECT_FACTORY.createMessaging();
        result.setUserMessage(userMessage);
        ebMSHeaderInfo.value = result;
        downloadMessageResponse.value = BackendWebServiceImpl.WEBSERVICE_OF.createDownloadMessageResponse();

        if (isMessageIdValued && result.getUserMessage() != null) {
            fillInfoParts(downloadMessageResponse, result);
        } else {
            LOG.info("Returning an empty response because the message id was empty.");
        }
    }

    private void fillInfoParts(Holder<DownloadMessageResponse> downloadMessageResponse, Messaging result) throws DownloadMessageFault {

        boolean mtomEnabled = false; // TODO get the property from WS configuration
        for (final PartInfo partInfo : result.getUserMessage().getPayloadInfo().getPartInfo()) {
            ExtendedPartInfo extPartInfo = (ExtendedPartInfo) partInfo;
            final PayloadType payloadType = BackendWebServiceImpl.WEBSERVICE_OF.createPayloadType();
            try {
                LOG.debug("downloadMessage - payloadDatahandler Content Type: " + extPartInfo.getPayloadDatahandler().getContentType());
                payloadType.setValue(IOUtils.toByteArray(extPartInfo.getPayloadDatahandler().getInputStream()));
            } catch (final IOException ioEx) {
                LOG.error(ERROR_IS_PAYLOAD_DATA_HANDLER, ioEx);
                throw new DownloadMessageFault(ERROR_IS_PAYLOAD_DATA_HANDLER, createDownloadMessageFault(ioEx));
            }
            JAXBElement<PayloadType> payloadElement = null;
            if (extPartInfo.isInBody()) {
                extPartInfo.setHref("#bodyload");
                payloadType.setPayloadId("#bodyload");
                payloadElement = BackendWebServiceImpl.WEBSERVICE_OF.createDownloadMessageResponseBodyload(payloadType);
            } else if (mtomEnabled) {
                payloadType.setPayloadId(partInfo.getHref());
                AnyPayloadType anyPayloadType = BackendWebServiceImpl.WEBSERVICE_OF.createAnyPayloadType();
                //Node item = null;
                Document xmlDoc = new DocumentImpl();
                Element root = xmlDoc.createElement("TODO");
                //item = xmlDoc.createElement("bookingID");
                //item.appendChild(xmlDoc.createTextNode(payloadType.toString());
                //root.appendChild(item);
                xmlDoc.appendChild(root);
                anyPayloadType.setAny(root);
                JAXBElement<AnyPayloadType> anyPayloadTypeElement = BackendWebServiceImpl.WEBSERVICE_OF.createPayload(anyPayloadType);
                downloadMessageResponse.value.getContent().add(anyPayloadTypeElement);
            } else {
                payloadType.setPayloadId(partInfo.getHref());
                payloadElement = BackendWebServiceImpl.WEBSERVICE_OF.createDownloadMessageResponsePayload(payloadType);
            }
            downloadMessageResponse.value.getContent().add(payloadElement);
        }
    }

    private FaultDetail createDownloadMessageFault(Exception ex) {
        FaultDetail detail = WEBSERVICE_OF.createFaultDetail();
        detail.setCode(eu.domibus.common.ErrorCode.EBMS_0004.getErrorCodeName());
        if (ex instanceof MessagingProcessingException) {
            MessagingProcessingException mpEx = (MessagingProcessingException) ex;
            detail.setCode(mpEx.getEbms3ErrorCode().getErrorCodeName());
            detail.setMessage(mpEx.getMessage());
        } else detail.setMessage(ex.getMessage());
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
