package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.core.message.fragment.MessageGroupEntity;
import eu.domibus.core.message.fragment.MessageHeaderEntity;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.mf.MessageFragmentType;
import eu.domibus.ebms3.common.model.mf.MessageHeaderType;
import eu.domibus.ebms3.common.model.mf.TypeType;
import eu.domibus.ebms3.sender.exception.SendMessageException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.transformer.impl.UserMessageFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service
public class EbMS3MessageBuilder {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EbMS3MessageBuilder.class);

    public static final String ID_PREFIX_MESSAGING = "_1";
    public static final String ID_PREFIX_SOAP_BODY = "_2";
    public static final String ID_PREFIX_MESSAGE_FRAGMENT = "_3";

    private final ObjectFactory ebMS3Of = new ObjectFactory();

    @Qualifier("messageFactory")
    @Autowired
    protected MessageFactory messageFactory;

    @Autowired
    @Qualifier(value = "jaxbContextEBMS")
    protected JAXBContext jaxbContext;

    @Autowired
    @Qualifier(value = "jaxbContextMessageFragment")
    protected JAXBContext jaxbContextMessageFragment;

    @Autowired
    protected DocumentBuilderFactory documentBuilderFactory;

    @Autowired
    protected MessageIdGenerator messageIdGenerator;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    public SOAPMessage buildSOAPMessage(final SignalMessage signalMessage, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPMessage(signalMessage);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public SOAPMessage buildSOAPMessage(final UserMessage userMessage, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPUserMessage(userMessage, null);
    }

    public SOAPMessage buildSOAPMessageForFragment(final UserMessage userMessage, MessageGroupEntity messageGroupEntity, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPUserMessage(userMessage, messageGroupEntity);
    }

    public SOAPMessage getSoapMessage(EbMS3Exception ebMS3Exception) {
        final SignalMessage signalMessage = new SignalMessage();
        signalMessage.getError().add(ebMS3Exception.getFaultInfoError());
        try {
            return buildSOAPMessage(signalMessage, null);
        } catch (EbMS3Exception e) {
            try {
                return buildSOAPFaultMessage(e.getFaultInfoError());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        }
    }

    //TODO: If Leg is used in future releases we have to update this method
    @Transactional(propagation = Propagation.SUPPORTS)
    public SOAPMessage buildSOAPFaultMessage(final Error ebMS3error) throws EbMS3Exception {
        final SignalMessage signalMessage = new SignalMessage();
        signalMessage.getError().add(ebMS3error);

        final SOAPMessage soapMessage = this.buildSOAPMessage(signalMessage, null);

        try {
            // An ebMS signal does not require any SOAP Body: if the SOAP Body is not empty, it MUST be ignored by the MSH, as far as interpretation of the signal is concerned.
            //TODO: locale is static
            soapMessage.getSOAPBody().addFault(SOAPConstants.SOAP_RECEIVER_FAULT, "An error occurred while processing your request. Please check the message header for more details.", Locale.ENGLISH);
        } catch (final SOAPException e) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "An error occurred while processing your request. Please check the message header for more details.", null, e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        return soapMessage;
    }

    protected SOAPMessage buildSOAPUserMessage(final UserMessage userMessage, MessageGroupEntity messageGroupEntity) throws EbMS3Exception {
        final SOAPMessage message;
        try {
            message = this.messageFactory.createMessage();
            final Messaging messaging = this.ebMS3Of.createMessaging();

            message.getSOAPBody().setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);

            String messageIDDigest = DigestUtils.sha256Hex(userMessage.getMessageInfo().getMessageId());
            message.getSOAPBody().addAttribute(NonRepudiationConstants.ID_QNAME, ID_PREFIX_SOAP_BODY + messageIDDigest);
            if (userMessage.getMessageInfo() != null && userMessage.getMessageInfo().getTimestamp() == null) {
                userMessage.getMessageInfo().setTimestamp(new Date());
            }

            for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
                this.attachPayload(partInfo, message);
            }
            if (messageGroupEntity != null) {
                final MessageFragmentType messageFragment = createMessageFragment(userMessage, messageGroupEntity);
                jaxbContextMessageFragment.createMarshaller().marshal(messageFragment, message.getSOAPHeader());

                final SOAPElement messageFragmentElement = (SOAPElement) message.getSOAPHeader().getChildElements(eu.domibus.ebms3.common.model.mf.ObjectFactory._MessageFragment_QNAME).next();
                messageFragmentElement.setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);
                messageFragmentElement.addAttribute(NonRepudiationConstants.ID_QNAME, ID_PREFIX_MESSAGE_FRAGMENT + messageIDDigest);

                messaging.setUserMessage(userMessageFactory.cloneUserMessageFragment(userMessage));
            } else {
                messaging.setUserMessage(userMessage);
            }

            this.jaxbContext.createMarshaller().marshal(messaging, message.getSOAPHeader());

            final SOAPElement messagingElement = (SOAPElement) message.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
            messagingElement.setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);
            messagingElement.addAttribute(NonRepudiationConstants.ID_QNAME, ID_PREFIX_MESSAGING + messageIDDigest);


            message.saveChanges();
        } catch (final SAXParseException e) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", userMessage.getMessageInfo().getMessageId(), e);
        } catch (final JAXBException | SOAPException | ParserConfigurationException | IOException | SAXException ex) {
            throw new SendMessageException(ex);
        }
        return message;
    }

    protected SOAPMessage buildSOAPMessage(final SignalMessage signalMessage) {
        final SOAPMessage message;
        try {
            message = this.messageFactory.createMessage();
            final Messaging messaging = this.ebMS3Of.createMessaging();

            if (signalMessage != null) {
                if (signalMessage.getMessageInfo() == null) {
                    final MessageInfo msgInfo = new MessageInfo();
                    String messageId = this.messageIdGenerator.generateMessageId();
                    msgInfo.setMessageId(messageId);
                    msgInfo.setTimestamp(new Date());
                    signalMessage.setMessageInfo(msgInfo);
                }

                if (signalMessage.getError() != null
                        && signalMessage.getError().iterator().hasNext()) {
                    signalMessage.getMessageInfo().setRefToMessageId(signalMessage.getError().iterator().next().getRefToMessageInError());
                }
                messaging.setSignalMessage(signalMessage);
            }
            this.jaxbContext.createMarshaller().marshal(messaging, message.getSOAPHeader());
            message.saveChanges();
        } catch (final JAXBException | SOAPException ex) {
            throw new SendMessageException(ex);
        }

        return message;
    }

    private void attachPayload(final PartInfo partInfo, final SOAPMessage message) throws ParserConfigurationException, SOAPException, IOException, SAXException {
        String mimeType = null;

        if (partInfo.getPartProperties() != null) {
            for (final Property prop : partInfo.getPartProperties().getProperties()) {
                if (Property.MIME_TYPE.equalsIgnoreCase(prop.getName())) {
                    mimeType = prop.getValue();
                }
            }
        }

        final DataHandler dataHandler = partInfo.getPayloadDatahandler();
        if (partInfo.isInBody() && mimeType != null && mimeType.toLowerCase().contains("xml")) { //TODO: respect empty soap body config
            this.documentBuilderFactory.setNamespaceAware(true);
            final DocumentBuilder builder = this.documentBuilderFactory.newDocumentBuilder();
            message.getSOAPBody().addDocument(builder.parse(dataHandler.getInputStream()));
            partInfo.setHref(null);
            return;
        }
        final AttachmentPart attachmentPart = message.createAttachmentPart(dataHandler);
        String href = partInfo.getHref();
        if (href.contains("cid:")) {
            href = href.substring(href.lastIndexOf("cid:") + "cid:".length());
        }
        if (!href.startsWith("<")) {
            href = "<" + href + ">";
        }
        attachmentPart.setContentId(href);
        attachmentPart.setContentType(partInfo.getMime());
        message.addAttachmentPart(attachmentPart);
    }

    public void setJaxbContext(final JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    protected MessageFragmentType createMessageFragment(UserMessage userMessageFragment, MessageGroupEntity messageGroupEntity) {
        MessageFragmentType result = new MessageFragmentType();

        result.setAction(messageGroupEntity.getSoapAction());

        final BigInteger compressedMessageSize = messageGroupEntity.getCompressedMessageSize();
        if (compressedMessageSize != null) {
            result.setCompressedMessageSize(compressedMessageSize);
        }
        final BigInteger messageSize = messageGroupEntity.getMessageSize();
        if (messageSize != null) {
            result.setMessageSize(messageSize);
        }
        result.setCompressionAlgorithm(messageGroupEntity.getCompressionAlgorithm());
        result.setFragmentCount(messageGroupEntity.getFragmentCount());
        result.setFragmentNum(userMessageFragment.getMessageFragment().getFragmentNumber());
        result.setGroupId(messageGroupEntity.getGroupId());
        result.setMustUnderstand(true);

        result.setMessageHeader(createMessageHeaderType(messageGroupEntity.getMessageHeaderEntity()));
        final PartInfo partInfo = userMessageFragment.getPayloadInfo().getPartInfo().iterator().next();
        result.setHref(partInfo.getHref());

        return result;
    }

    protected MessageHeaderType createMessageHeaderType(MessageHeaderEntity messageHeaderEntity) {
        MessageHeaderType messageHeader = new MessageHeaderType();
        messageHeader.setBoundary(messageHeaderEntity.getBoundary());
        messageHeader.setStart(messageHeaderEntity.getStart());
        messageHeader.setContentType("Multipart/Related");
        messageHeader.setType(TypeType.TEXT_XML);
        return messageHeader;
    }
}
