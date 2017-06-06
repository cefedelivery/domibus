package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.UserMessageLogBuilder;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.cxf.attachment.AttachmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * Created by dussath on 6/6/17.
 *
 */
@Component
public class UserMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageHandler.class);
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
    private PayloadProfileValidator payloadProfileValidator;
    @Autowired
    private PropertyProfileValidator propertyProfileValidator;
    @Autowired
    private MessagingService messagingService;

    public void handleNewUserMessage(final String pmodeKey, final SOAPMessage request, final Messaging messaging) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pmodeKey);
        boolean pingMessage = false;

        String messageId = null;
        try (StringWriter sw = new StringWriter()) {
            if (LOG.isDebugEnabled()) {

                transformerFactory.newTransformer().transform(new DOMSource(request.getSOAPPart()), new StreamResult(sw));

                LOG.debug(sw.toString());
                LOG.debug("received attachments:");
                final Iterator i = request.getAttachments();
                while (i.hasNext()) {
                    LOG.debug("attachment: " + i.next());
                }
            }

            messageId = messaging.getUserMessage().getMessageInfo().getMessageId();

            checkCharset(messaging);
            pingMessage = checkPingMessage(messaging.getUserMessage());
            final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(messaging);
            LOG.debug("Message duplication status:{}", messageExists);
            if (!messageExists && !pingMessage) { // ping messages are not stored/delivered
                persistReceivedMessage(request, legConfiguration, pmodeKey, messaging);
                try {
                    backendNotificationService.notifyMessageReceived(messaging.getUserMessage());
                } catch (SubmissionValidationException e) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), messageId, e);
                }
            }

        }
    }
    /**
     * Required for AS4_TA_12
     *
     * @param messaging
     * @throws EbMS3Exception
     */
    protected void checkCharset(final Messaging messaging) throws EbMS3Exception {
        LOG.info("Checking charset for attachments");
        for (final PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
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
     * Check if this message is a ping message
     *
     * @param message
     * @return result of ping service and action handle
     */
    protected Boolean checkPingMessage(final UserMessage message) {
        LOG.debug("Checking if it is a ping message");
        return Ebms3Constants.TEST_SERVICE.equals(message.getCollaborationInfo().getService().getValue())
                && Ebms3Constants.TEST_ACTION.equals(message.getCollaborationInfo().getAction());

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
    protected String persistReceivedMessage(final SOAPMessage request, final LegConfiguration legConfiguration, final String pmodeKey, final Messaging messaging) throws SOAPException, JAXBException, TransformerException, EbMS3Exception {
        LOG.info("Persisting received message");
        UserMessage userMessage = messaging.getUserMessage();

        handlePayloads(request, userMessage);

        boolean compressed = compressionService.handleDecompression(userMessage, legConfiguration);
        LOG.debug("Compression for message with id: " + userMessage.getMessageInfo().getMessageId() + " applied: " + compressed);
        try {
            payloadProfileValidator.validate(messaging, pmodeKey);
            propertyProfileValidator.validate(messaging, pmodeKey);
        } catch (EbMS3Exception e) {
            e.setMshRole(MSHRole.RECEIVING);
            throw e;
        }

        try {
            messagingService.storeMessage(messaging);
        } catch (CompressionException exc) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "Could not persist message" + exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        Party to = pModeProvider.getReceiverParty(pmodeKey);
        Validate.notNull(to, "Responder party was not found");

        // Builds the user message log
        UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                .setMessageId(userMessage.getMessageInfo().getMessageId())
                .setMessageStatus(MessageStatus.RECEIVED)
                .setMshRole(MSHRole.RECEIVING)
                .setNotificationStatus(legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer() ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED)
                .setMpc(StringUtils.isEmpty(userMessage.getMpc()) ? Ebms3Constants.DEFAULT_MPC : userMessage.getMpc())
                .setSendAttemptsMax(0)
                .setBackendName(getFinalRecipientName(userMessage))
                .setEndpoint(to.getEndpoint());
        // Saves the user message log
        userMessageLogDao.create(umlBuilder.build());

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PERSISTED);

        return userMessage.getMessageInfo().getMessageId();
    }

    /**
     * If message with same messageId is already in the database return <code>true</code> else <code>false</code>
     *
     * @param messaging
     * @return result of duplicate handle
     */
    protected Boolean checkDuplicate(final Messaging messaging) {
        LOG.debug("Checking for duplicate messages");
        return userMessageLogDao.findByMessageId(messaging.getUserMessage().getMessageInfo().getMessageId(), MSHRole.RECEIVING) != null;
    }

    protected void handlePayloads(SOAPMessage request, UserMessage userMessage) throws EbMS3Exception, SOAPException, TransformerException {
        boolean bodyloadFound = false;
        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            final String cid = partInfo.getHref();
            LOG.debug("looking for attachment with cid: " + cid);
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
                final Node bodyContent = (((Node) request.getSOAPBody().getChildElements().next()));
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

    protected String getFinalRecipientName(UserMessage userMessage) {
        for (Property property : userMessage.getMessageProperties().getProperty()) {
            if (property.getName() != null && property.getName().equals(MessageConstants.FINAL_RECIPIENT)) {
                return property.getValue();
            }
        }
        return null;
    }
}
