package eu.domibus.common.services.impl;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.*;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.ErrorHandling;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.core.nonrepudiation.NonRepudiationService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.receiver.UserMessageHandlerContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.util.MessageUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.attachment.AttachmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Iterator;

/**
 * @author Thomas Dussart
 * @author Catalin Enache
 * @since 3.3
 *
 */
@org.springframework.stereotype.Service
public class UserMessageHandlerService {

    private static final String XSLT_GENERATE_AS4_RECEIPT_XSL = "xslt/GenerateAS4Receipt.xsl";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageHandlerService.class);

    /** to be appended to messageId when saving to DB on receiver side */
    public static final String SELF_SENDING_SUFFIX = "_1";


    private byte[] as4ReceiptXslBytes;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private TransformerFactory transformerFactory;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    @Autowired
    private MessagingService messagingService;

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

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    @Autowired
    protected NonRepudiationService nonRepudiationService;


    public SOAPMessage handleNewUserMessage(final String pmodeKey, final SOAPMessage request, final Messaging messaging,final UserMessageHandlerContext userMessageHandlerContext) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pmodeKey);
        userMessageHandlerContext.setLegConfiguration(legConfiguration);
        String messageId;
        try (StringWriter sw = new StringWriter()) {
            if (LOG.isDebugEnabled()) {

                transformerFactory.newTransformer().transform(new DOMSource(request.getSOAPPart()), new StreamResult(sw));

                LOG.debug(sw.toString());
                LOG.debug("received attachments:");
                final Iterator i = request.getAttachments();
                while (i.hasNext()) {
                    LOG.debug("attachment: {}", i.next());
                }
            }

            //check if the message is sent to the same Domibus instance
            final boolean selfSendingFlag = checkSelfSending(pmodeKey);
            if (selfSendingFlag) {
                /* we add a defined suffix in order to assure DB integrity - messageId unicity
                basically we are generating another messageId for Signal Message on receievr side
                */
                messaging.getUserMessage().getMessageInfo().setMessageId(messaging.getUserMessage().getMessageInfo().getMessageId() + SELF_SENDING_SUFFIX);
            }

            messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
            userMessageHandlerContext.setMessageId(messageId);

            checkCharset(messaging);
            boolean testMessage = checkTestMessage(messaging.getUserMessage());
            userMessageHandlerContext.setTestMessage(testMessage);
            final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(messaging);
            LOG.debug("Message duplication status:{}", messageExists);
            if (!messageExists) {
                if(testMessage) {
                    // ping messages are only stored and not notified to the plugins
                    persistReceivedMessage(request, legConfiguration, pmodeKey, messaging, null);
                } else {
                    final BackendFilter matchingBackendFilter = backendNotificationService.getMatchingBackendFilter(messaging.getUserMessage());
                    String backendName = (matchingBackendFilter != null ? matchingBackendFilter.getBackendName() : null);
                    persistReceivedMessage(request, legConfiguration, pmodeKey, messaging, backendName);
                    try {
                        backendNotificationService.notifyMessageReceived(matchingBackendFilter, messaging.getUserMessage());
                    } catch (SubmissionValidationException e) {
                        LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
                        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), messageId, e);
                    }
                }
            }
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED, messageId);
            return generateReceipt(request, legConfiguration, messageExists, selfSendingFlag);
        }
    }

    /**
     * It will check if the messages are sent to the same Domibus instance
     *
     * @param pmodeKey pmode key
     * @return boolean true if there is the same AP
     */
    protected boolean checkSelfSending(String pmodeKey) {
        final Party receiver = pModeProvider.getReceiverParty(pmodeKey);
        final Party sender = pModeProvider.getSenderParty(pmodeKey);

        //check endpoint
        if (receiver.getEndpoint().trim().equalsIgnoreCase(sender.getEndpoint().trim())) {
            return true;
        }

        return false;
    }

    /**
     * Required for AS4_TA_12
     *
     * @param messaging the UserMessage received
     * @throws EbMS3Exception if an attachment with an invalid charset is received
     */
    protected void checkCharset(final Messaging messaging) throws EbMS3Exception {
        LOG.info("Checking charset for attachments");
        for (final PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            if(partInfo.getPartProperties() == null || partInfo.getPartProperties().getProperties() == null) {
                continue;
            }
            for (final Property property : partInfo.getPartProperties().getProperties()) {
                if (Property.CHARSET.equals(property.getName()) && !Property.CHARSET_PATTERN.matcher(property.getValue()).matches()) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, property.getValue(), messaging.getUserMessage().getMessageInfo().getMessageId());
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, property.getValue() + " is not a valid Charset", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
                    ex.setMshRole(MSHRole.RECEIVING);
                    throw ex;
                }
            }
        }
    }

    /**
     * Check if this message is a test message
     *
     * @param message the message
     * @return result of test service and action handle
     */
    public Boolean checkTestMessage(final UserMessage message) {
        LOG.debug("Checking if it is a test message");
        return Ebms3Constants.TEST_SERVICE.equals(message.getCollaborationInfo().getService().getValue())
                && Ebms3Constants.TEST_ACTION.equals(message.getCollaborationInfo().getAction());

    }

    /**
     * Check if this message is a test message
     *
     * @param legConfiguration the legConfiguration that matched the message
     * @return result of test service and action handle
     */
    protected Boolean checkTestMessage(final LegConfiguration legConfiguration) {
        LOG.debug("Checking if it is a test message");
        return Ebms3Constants.TEST_SERVICE.equals(legConfiguration.getService().getValue())
                && Ebms3Constants.TEST_ACTION.equals(legConfiguration.getAction());

    }

    /**
     * This method persists incoming messages into the database (and handles decompression before)
     *
     * @param request          the message to persist
     * @param legConfiguration processing information for the message
     * @throws SOAPException
     * @throws JAXBException
     * @throws TransformerException
     * @throws IOException
     * @throws EbMS3Exception
     */
    //TODO: improve error handling
    String persistReceivedMessage(final SOAPMessage request, final LegConfiguration legConfiguration, final String pmodeKey, final Messaging messaging, final String backendName) throws SOAPException, TransformerException, EbMS3Exception {
        LOG.info("Persisting received message");
        UserMessage userMessage = messaging.getUserMessage();

        handlePayloads(request, userMessage);

        boolean compressed = compressionService.handleDecompression(userMessage, legConfiguration);
        LOG.debug("Compression for message with id: {} applied: {}", userMessage.getMessageInfo().getMessageId(), compressed);
        try {
            payloadProfileValidator.validate(messaging, pmodeKey);
            propertyProfileValidator.validate(messaging, pmodeKey);
        } catch (EbMS3Exception e) {
            e.setMshRole(MSHRole.RECEIVING);
            throw e;
        }

        try {
            messagingService.storeMessage(messaging, MSHRole.RECEIVING);
        } catch (CompressionException exc) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "Could not persist message" + exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        Party to = pModeProvider.getReceiverParty(pmodeKey);
        Validate.notNull(to, "Responder party was not found");

        NotificationStatus notificationStatus = (legConfiguration.getErrorHandling() != null &&legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED;
        LOG.debug("NotificationStatus [{}]", notificationStatus);

        userMessageLogService.save(
                userMessage.getMessageInfo().getMessageId(),
                MessageStatus.RECEIVED.toString(),
                notificationStatus.toString(),
                MSHRole.RECEIVING.toString(),
                0,
                StringUtils.isEmpty(userMessage.getMpc()) ? Ebms3Constants.DEFAULT_MPC : userMessage.getMpc(),
                backendName,
                to.getEndpoint(),
                userMessage.getCollaborationInfo().getService().getValue(),
                userMessage.getCollaborationInfo().getAction());

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PERSISTED);

        nonRepudiationService.saveRequest(request, userMessage);

        return userMessage.getMessageInfo().getMessageId();
    }

    /**
     * If message with same messageId is already in the database return <code>true</code> else <code>false</code>
     *
     * @param messaging the message
     * @return result of duplicate handle
     */
    Boolean checkDuplicate(final Messaging messaging) {
        LOG.debug("Checking for duplicate messages");
        return userMessageLogDao.findByMessageId(messaging.getUserMessage().getMessageInfo().getMessageId(), MSHRole.RECEIVING) != null;
    }

    void handlePayloads(SOAPMessage request, UserMessage userMessage) throws EbMS3Exception, SOAPException, TransformerException {
        boolean bodyloadFound = false;
        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            final String cid = partInfo.getHref();
            LOG.debug("looking for attachment with cid: {}", cid);
            boolean payloadFound = false;
            if (cid == null || cid.isEmpty() || cid.startsWith("#")) {
                if (bodyloadFound) {
                    LOG.businessError(DomibusMessageCode.BUS_MULTIPLE_PART_INFO_REFERENCING_SOAP_BODY);
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "More than one Partinfo referencing the soap body found", userMessage.getMessageInfo().getMessageId(), null);
                    ex.setMshRole(MSHRole.RECEIVING);
                    throw ex;
                }
                LOG.info("Using soap body payload");
                bodyloadFound = true;
                payloadFound = true;
                partInfo.setInBody(true);
                final Node bodyContent = ((Node) request.getSOAPBody().getChildElements().next());
                final Source source = new DOMSource(bodyContent);
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final Result result = new StreamResult(out);
                final Transformer transformer = this.transformerFactory.newTransformer();
                transformer.transform(source, result);
                partInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(out.toByteArray(), "text/xml")));
            }
            @SuppressWarnings("unchecked") final Iterator<AttachmentPart> attachmentIterator = request.getAttachments();
            AttachmentPart attachmentPart;
            while (attachmentIterator.hasNext() && !payloadFound) {

                attachmentPart = attachmentIterator.next();
                //remove square brackets from cid for further processing
                attachmentPart.setContentId(AttachmentUtil.cleanContentId(attachmentPart.getContentId()));
                LOG.debug("comparing with: " + attachmentPart.getContentId());
                if (attachmentPart.getContentId().equals(AttachmentUtil.cleanContentId(cid))) {
                    partInfo.setPayloadDatahandler(attachmentPart.getDataHandler());
                    partInfo.setInBody(false);
                    payloadFound = true;
                }
            }
            if (!payloadFound) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_ATTACHMENT_NOT_FOUND, cid);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0011, "No Attachment found for cid: " + cid + " of message: " + userMessage.getMessageInfo().getMessageId(), userMessage.getMessageInfo().getMessageId(), null);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }
    }

    String getFinalRecipientName(UserMessage userMessage) {
        for (Property property : userMessage.getMessageProperties().getProperty()) {
            if (property.getName() != null && property.getName().equals(MessageConstants.FINAL_RECIPIENT)) {
                return property.getValue();
            }
        }
        return null;
    }

    /**
     * Handles Receipt generation for a incoming message
     *
     * @param request          the incoming message
     * @param legConfiguration processing information of the message
     * @param duplicate        indicates whether or not the message is a duplicate
     * @param selfSendingFlag indicates that the message is sent to the same Domibus instance
     * @return the response message to the incoming request message
     * @throws EbMS3Exception if generation of receipt was not successful
     */
    SOAPMessage generateReceipt(final SOAPMessage request, final LegConfiguration legConfiguration, final Boolean duplicate,
                                boolean selfSendingFlag) throws EbMS3Exception {

        SOAPMessage responseMessage = null;
        assert legConfiguration != null;

        if (legConfiguration.getReliability() == null) {
            LOG.warn("No reliability found for leg [{}]", legConfiguration.getName());
            return null;
        }

        if (ReplyPattern.RESPONSE.equals(legConfiguration.getReliability().getReplyPattern())) {
            LOG.info("Generating receipt for incoming message");
            try {
                responseMessage = messageFactory.createMessage();
                InputStream generateAS4ReceiptStream = getAs4ReceiptXslInputStream();
                Source messageToReceiptTransform = new StreamSource(generateAS4ReceiptStream);
                final Transformer transformer = this.transformerFactory.newTransformer(messageToReceiptTransform);
                final Source requestMessage = request.getSOAPPart().getContent();
                transformer.setParameter("messageid", this.messageIdGenerator.generateMessageId());
                transformer.setParameter("timestamp", this.timestampDateFormatter.generateTimestamp());
                transformer.setParameter("nonRepudiation", Boolean.toString(legConfiguration.getReliability().isNonRepudiation()));

                final DOMResult domResult = new DOMResult();
                transformer.transform(requestMessage, domResult);
                responseMessage.getSOAPPart().setContent(new DOMSource(domResult.getNode()));
                saveResponse(responseMessage, selfSendingFlag);

                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIPT_GENERATED, legConfiguration.getReliability().isNonRepudiation());
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

    public ErrorResult createErrorResult(EbMS3Exception ebm3Exception) {
        ErrorResultImpl result = new ErrorResultImpl();
        result.setMshRole(MSHRole.RECEIVING);
        result.setMessageInErrorId(ebm3Exception.getRefToMessageId());
        try {
            result.setErrorCode(ebm3Exception.getErrorCodeObject());
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not find error code for [" + ebm3Exception.getErrorCode() + "]");
        }
        result.setErrorDetail(ebm3Exception.getErrorDetail());
        return result;
    }

    /**
     * save response in the DB before sending it back
     *
     * @param responseMessage SOAP response message
     * @param selfSendingFlag indicates that the message is sent to the same Domibus instance
     */
    void saveResponse(final SOAPMessage responseMessage, boolean selfSendingFlag) {
        try {
            Messaging messaging = getMessaging(responseMessage);
            final SignalMessage signalMessage = messaging.getSignalMessage();

            if (selfSendingFlag) {
                /*we add a defined suffix in order to assure DB integrity - messageId unicity
                basically we are generating another messageId for Signal Message on receievr side
                */
                signalMessage.getMessageInfo().setRefToMessageId(signalMessage.getMessageInfo().getRefToMessageId() + SELF_SENDING_SUFFIX);
                signalMessage.getMessageInfo().setMessageId(signalMessage.getMessageInfo().getMessageId() + SELF_SENDING_SUFFIX);
            }
            // Stores the signal message
            signalMessageDao.create(signalMessage);
            // Updating the reference to the signal message
            Messaging sentMessage = messagingDao.findMessageByMessageId(messaging.getSignalMessage().getMessageInfo().getRefToMessageId());
            MessageSubtype messageSubtype = null;
            if (sentMessage != null) {
                if (checkTestMessage(sentMessage.getUserMessage())) {
                    messageSubtype = MessageSubtype.TEST;
                }
                sentMessage.setSignalMessage(signalMessage);
                messagingDao.update(sentMessage);
            }
            // Builds the signal message log
            SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                    .setMessageId(messaging.getSignalMessage().getMessageInfo().getMessageId())
                    .setMessageStatus(MessageStatus.SEND_IN_PROGRESS)
                    .setMshRole(MSHRole.SENDING)
                    .setNotificationStatus(NotificationStatus.NOT_REQUIRED);
            // Saves an entry of the signal message log
            SignalMessageLog signalMessageLog = smlBuilder.build();
            signalMessageLog.setMessageSubtype(messageSubtype);
            signalMessageLogDao.create(signalMessageLog);
        } catch (JAXBException | SOAPException ex) {
            LOG.error("Unable to save the SignalMessage due to error: ", ex);
        }

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

    public Messaging getMessaging(final SOAPMessage request) throws SOAPException, JAXBException {
        LOG.debug("Unmarshalling the Messaging instance from the request");
        return MessageUtil.getMessaging(request, jaxbContext);
    }

}
