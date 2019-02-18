package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.exception.SendMessageException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.OutgoingMessageTransformer;
import eu.domibus.plugin.transformer.OutgoingMessageTransformerList;
import eu.domibus.plugin.transformer.PluginHandler;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import eu.domibus.submission.plugin.PluginHandlerProvider;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service
public class EbMS3MessageBuilder {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EbMS3MessageBuilder.class);
    private final ObjectFactory ebMS3Of = new ObjectFactory();

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessageFactory messageFactory;

    @Autowired
    @Qualifier(value = "jaxbContextEBMS")
    private JAXBContext jaxbContext;

    @Autowired
    private DocumentBuilderFactory documentBuilderFactory;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    protected PluginHandlerProvider pluginHandlerProvider;

    @Autowired
    protected SubmissionAS4Transformer submissionAS4Transformer;

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

    protected List<String> extractSamlTokens(UserMessage userMessage) {
        List<String> result = new ArrayList<>();

        final MessageProperties messageProperties = userMessage.getMessageProperties();
        final Set<Property> properties = messageProperties.getProperty();
        final Iterator<Property> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property next = iterator.next();
            if(next.getName().contains("saml")) {
                LOG.info("Removing saml property [{}]", next.getName());
                result.add(new String(next.getValueBlob(), StandardCharsets.UTF_8));
                iterator.remove();
            }
        }

        return result;
    }

    protected SOAPMessage buildSOAPMessage(final UserMessage userMessage) throws EbMS3Exception {
        final SOAPMessage message;
        try {
            message = this.messageFactory.createMessage();
            final Messaging messaging = this.ebMS3Of.createMessaging();

            message.getSOAPBody().setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);

            String messageIDDigest = DigestUtils.sha256Hex(userMessage.getMessageInfo().getMessageId());
            message.getSOAPBody().addAttribute(NonRepudiationConstants.ID_QNAME, "_2" + messageIDDigest);
            if (userMessage.getMessageInfo() != null && userMessage.getMessageInfo().getTimestamp() == null) {
                userMessage.getMessageInfo().setTimestamp(new Date());
            }

            messaging.setUserMessage(userMessage);
            for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
                this.attachPayload(partInfo, message);
            }

            final List<String> samlTokens = extractSamlTokens(messaging.getUserMessage());
            this.jaxbContext.createMarshaller().marshal(messaging, message.getSOAPHeader());

            transformBeforeSending(userMessage, message, samlTokens);

            final SOAPElement messagingElement = (SOAPElement) message.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
            messagingElement.setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);
            messagingElement.addAttribute(NonRepudiationConstants.ID_QNAME, "_1" + messageIDDigest);

            message.saveChanges();
        } catch (final SAXParseException e) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", userMessage.getMessageInfo().getMessageId(), e);
        } catch (final JAXBException | SOAPException | ParserConfigurationException | IOException | SAXException ex) {
            throw new SendMessageException(ex);
        }
        return message;
    }

    protected void transformBeforeSending(UserMessage userMessage, SOAPMessage message, List<String> samlTokens) {
        String messageId = userMessage.getMessageInfo().getMessageId();
        final String backendName = userMessageLogDao.findBackendForMessageId(messageId);
        final PluginHandler pluginHandler = pluginHandlerProvider.getPluginHandler(backendName);
        if (pluginHandler == null) {
            LOG.debug("No plugin handler found for backend [" + backendName + "]");
            return;
        }

        final OutgoingMessageTransformerList outgoingMessageTransformerList = pluginHandler.getOutgoingMessageTransformerList();
        if (outgoingMessageTransformerList == null) {
            LOG.debug("No outgoing message transformer found for backend [" + backendName + "]");
            return;
        }

        final Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage);
        for (int i = 0; i < samlTokens.size(); i++) {
            final String samlTokenValue = samlTokens.get(i);
            submission.addMessageProperty("saml" + i, samlTokenValue );

        }

        final List<OutgoingMessageTransformer> outgoingMessageTransformers = outgoingMessageTransformerList.getOutgoingMessageTransformers();
        for (OutgoingMessageTransformer outgoingMessageTransformer : outgoingMessageTransformers) {
            outgoingMessageTransformer.transformOutgoingMessage(submission, message);
        }
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
