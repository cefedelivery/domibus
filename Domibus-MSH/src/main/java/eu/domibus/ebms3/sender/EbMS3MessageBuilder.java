package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.exception.SendMessageException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service
public class EbMS3MessageBuilder {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EbMS3MessageBuilder.class);
    private final ObjectFactory ebMS3Of = new ObjectFactory();

    @Autowired
    private MessageFactory messageFactory;

    @Autowired
    @Qualifier(value = "jaxbContextEBMS")
    private JAXBContext jaxbContext;

    @Autowired
    private DocumentBuilderFactory documentBuilderFactory;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    public void setJaxbContext(final JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public SOAPMessage buildSOAPMessage(final SignalMessage signalMessage, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPMessage(signalMessage);
    }

    public SOAPMessage buildSOAPMessage(final UserMessage userMessage, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPMessage(userMessage);
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

    protected SOAPMessage buildSOAPMessage(final UserMessage userMessage) throws EbMS3Exception {
        final SOAPMessage message;
        try {
            message = this.messageFactory.createMessage();
            final Messaging messaging = this.ebMS3Of.createMessaging();

            final String qualifiedName = "xmlns:wsu";
            final String namespace = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
            message.getSOAPBody().setAttributeNS("http://www.w3.org/2000/xmlns/", qualifiedName, namespace);
            QName idQname = new QName(namespace, "Id", "wsu");

            String messageIDDigest = DigestUtils.sha256Hex(userMessage.getMessageInfo().getMessageId());
            message.getSOAPBody().addAttribute(idQname, "_2" + messageIDDigest);
            if (userMessage.getMessageInfo() != null && userMessage.getMessageInfo().getTimestamp() == null) {
                userMessage.getMessageInfo().setTimestamp(new Date());
            }

            messaging.setUserMessage(userMessage);
            for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
                this.attachPayload(partInfo, message);
            }

            this.jaxbContext.createMarshaller().marshal(messaging, message.getSOAPHeader());
            final Object next = message.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
            final SOAPElement next1 = (SOAPElement) next;
            next1.setAttributeNS("http://www.w3.org/2000/xmlns/", qualifiedName, namespace);
            next1.addAttribute(idQname, "_1" + messageIDDigest);

            message.saveChanges();
        } catch (final SAXParseException e) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", userMessage.getMessageInfo().getMessageId(), e);
        } catch (final JAXBException | SOAPException | ParserConfigurationException | IOException | SAXException ex) {
            throw new SendMessageException(ex);
        }
        return message;
    }

    protected SOAPMessage buildSOAPMessage(final SignalMessage signalMessage) throws EbMS3Exception {
        final SOAPMessage message;
        try {
            message = this.messageFactory.createMessage();
            final Messaging messaging = this.ebMS3Of.createMessaging();

            if (signalMessage != null) {
                final MessageInfo msgInfo = new MessageInfo();

                String messageId = this.messageIdGenerator.generateMessageId();
                msgInfo.setMessageId(messageId);
                msgInfo.setTimestamp(new Date());
                if (signalMessage.getError() != null && signalMessage.getError().iterator().hasNext()) {
                    msgInfo.setRefToMessageId(signalMessage.getError().iterator().next().getRefToMessageInError());
                }

                signalMessage.setMessageInfo(msgInfo);
            }
            messaging.setSignalMessage(signalMessage);
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
}
