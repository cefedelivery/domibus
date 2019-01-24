package eu.domibus.common.services.impl;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.core.message.fragment.MessageGroupDao;
import eu.domibus.core.nonrepudiation.NonRepudiationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class AS4ReceiptServiceImpl implements AS4ReceiptService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AS4ReceiptServiceImpl.class);

    private static final String XSLT_GENERATE_AS4_RECEIPT_XSL = "xslt/GenerateAS4Receipt.xsl";

    protected byte[] as4ReceiptXslBytes;

    @Autowired
    private TransformerFactory transformerFactory;


    @Qualifier("messageFactory")
    @Autowired
    private MessageFactory messageFactory;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private TimestampDateFormatter timestampDateFormatter;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    protected NonRepudiationService nonRepudiationService;

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    @Autowired
    protected SoapUtil soapUtil;

    @Override
    public SOAPMessage generateReceipt(String messageId, final Boolean nonRepudiation) throws EbMS3Exception {
        final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(messageId);
        SOAPMessage request = null;
        try {
            request = soapUtil.createSOAPMessage(rawXmlByMessageId.getRawMessage());
        } catch (SOAPException | IOException | ParserConfigurationException | SAXException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0201, "Could not generate Receipt. Check security header and non-repudiation settings", null, e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        Messaging messaging = messagingDao.findMessageByMessageId(messageId);
        return generateReceipt(request, messaging, ReplyPattern.RESPONSE, nonRepudiation, false, false);
    }

    @Override
    public SOAPMessage generateReceipt(final SOAPMessage request,
                                       final Messaging messaging,
                                       final ReplyPattern replyPattern,
                                       final Boolean nonRepudiation,
                                       final Boolean duplicate,
                                       final boolean selfSendingFlag) throws EbMS3Exception {
        SOAPMessage responseMessage = null;
        UserMessage userMessage = messaging.getUserMessage();

        if (ReplyPattern.RESPONSE.equals(replyPattern)) {
            LOG.debug("Generating receipt for incoming message");
            try {
                responseMessage = messageFactory.createMessage();
                InputStream generateAS4ReceiptStream = getAs4ReceiptXslInputStream();
                Source messageToReceiptTransform = new StreamSource(generateAS4ReceiptStream);
                final Transformer transformer = this.transformerFactory.newTransformer(messageToReceiptTransform);

                String messageId;
                String timestamp;

                Source requestMessage;
                if (duplicate) {
                    final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(userMessage.getMessageInfo().getMessageId());
                    Messaging existingMessage = messagingDao.findMessageByMessageId(userMessage.getMessageInfo().getMessageId());
                    messageId = existingMessage.getSignalMessage().getMessageInfo().getMessageId();
                    timestamp = timestampDateFormatter.generateTimestamp(existingMessage.getSignalMessage().getMessageInfo().getTimestamp());
                    requestMessage = new StreamSource(new StringReader(rawXmlByMessageId.getRawMessage()));
                } else {
                    messageId = messageIdGenerator.generateMessageId();
                    timestamp = timestampDateFormatter.generateTimestamp();
                    requestMessage = request.getSOAPPart().getContent();
                }

                transformer.setParameter("messageid", messageId);
                transformer.setParameter("timestamp", timestamp);
                transformer.setParameter("nonRepudiation", Boolean.toString(nonRepudiation));

                final DOMResult domResult = new DOMResult();
                transformer.transform(requestMessage, domResult);
                responseMessage.getSOAPPart().setContent(new DOMSource(domResult.getNode()));

                setMessagingId(responseMessage, userMessage);

                if (!duplicate) {
                    saveResponse(responseMessage, selfSendingFlag);
                }

                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIPT_GENERATED, nonRepudiation);
            } catch (TransformerConfigurationException | SOAPException | IOException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
                // this cannot happen
                assert false;
                throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Error generating receipt", e);
            } catch (final TransformerException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0201, "Could not generate Receipt. Check security header and non-repudiation settings", null, e);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }
        return responseMessage;
    }

    /**
     * save response in the DB before sending it back
     *
     * @param responseMessage SOAP response message
     * @param selfSendingFlag indicates that the message is sent to the same Domibus instance
     */
    protected void saveResponse(final SOAPMessage responseMessage, boolean selfSendingFlag) {
        try {
            Messaging messaging = messageUtil.getMessaging(responseMessage);
            final SignalMessage signalMessage = messaging.getSignalMessage();

            if (selfSendingFlag) {
                /*we add a defined suffix in order to assure DB integrity - messageId unicity
                basically we are generating another messageId for Signal Message on receievr side
                */
                signalMessage.getMessageInfo().setRefToMessageId(signalMessage.getMessageInfo().getRefToMessageId() + UserMessageHandlerService.SELF_SENDING_SUFFIX);
                signalMessage.getMessageInfo().setMessageId(signalMessage.getMessageInfo().getMessageId() + UserMessageHandlerService.SELF_SENDING_SUFFIX);
            }
            // Stores the signal message
            signalMessageDao.create(signalMessage);
            // Updating the reference to the signal message
            Messaging sentMessage = messagingDao.findMessageByMessageId(messaging.getSignalMessage().getMessageInfo().getRefToMessageId());
            MessageSubtype messageSubtype = null;
            if (sentMessage != null) {
                if (userMessageHandlerService.checkTestMessage(sentMessage.getUserMessage())) {
                    messageSubtype = MessageSubtype.TEST;
                }
                sentMessage.setSignalMessage(signalMessage);
                messagingDao.update(sentMessage);
            }
            // Builds the signal message log
            SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                    .setMessageId(messaging.getSignalMessage().getMessageInfo().getMessageId())
                    .setMessageStatus(MessageStatus.ACKNOWLEDGED)
                    .setMshRole(MSHRole.SENDING)
                    .setNotificationStatus(NotificationStatus.NOT_REQUIRED);
            // Saves an entry of the signal message log
            SignalMessageLog signalMessageLog = smlBuilder.build();
            signalMessageLog.setMessageSubtype(messageSubtype);
            signalMessageLogDao.create(signalMessageLog);

            uiReplicationSignalService.signalMessageSubmitted(signalMessageLog.getMessageId());
        } catch (JAXBException | SOAPException ex) {
            LOG.error("Unable to save the SignalMessage due to error: ", ex);
        }

    }


    protected void setMessagingId(SOAPMessage responseMessage, UserMessage userMessage) throws SOAPException {
        final Iterator childElements = responseMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
        if (childElements == null || !childElements.hasNext()) {
            LOG.warn("Could not set the Messaging Id value");
            return;
        }

        final SOAPElement messagingElement = (SOAPElement) childElements.next();
        messagingElement.addAttribute(NonRepudiationConstants.ID_QNAME, "_1" + DigestUtils.sha256Hex(userMessage.getMessageInfo().getMessageId()));
    }

    protected InputStream getAs4ReceiptXslInputStream() throws IOException {
        return new ByteArrayInputStream(getAs4ReceiptXslBytes());
    }

    protected byte[] getAs4ReceiptXslBytes() throws IOException {
        if (as4ReceiptXslBytes == null) {
            as4ReceiptXslBytes = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(XSLT_GENERATE_AS4_RECEIPT_XSL));
        }
        return as4ReceiptXslBytes;
    }
}
