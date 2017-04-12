package eu.domibus.plugin.handler;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.*;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.exception.MessagingExceptionFactory;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.model.logging.UserMessageLogBuilder;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.*;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible of handling the plugins requests for all the operations exposed.
 * During submit, it manages the user authentication and the AS4 message's validation, compression and saving.
 * During download, it manages the user authentication and the AS4 message's reading, data clearing and status update.
 *
 * @author Christian Koch, Stefan Mueller, Federico Martini, Ioana Dragusanu
 * @Since 3.0
 */
@Service
public class DatabaseMessageHandler implements MessageSubmitter<Submission>, MessageRetriever<Submission> {

    private static final Log LOG = LogFactory.getLog(DatabaseMessageHandler.class);

    private final ObjectFactory objectFactory = new ObjectFactory();

    private final ObjectFactory ebMS3Of = new ObjectFactory();

    @Autowired
    JMSManager jmsManager;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue sendMessageQueue;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private SubmissionAS4Transformer transformer;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    @Autowired
    AuthUtils authUtils;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasUserOrAdminRole();

        String originalUser = authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
        LOG.debug("Authorized as " + (originalUser == null ? "super user" : originalUser));

        LOG.info("Searching message with id [" + messageId + "]");
        final UserMessageLog userMessageLog;
        final UserMessage userMessage;
        try {
            userMessage = messagingDao.findUserMessageByMessageId(messageId);
            userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.RECEIVING);
            if (userMessageLog == null) {
                throw new MessageNotFoundException("Message with id [" + messageId + "] was not found");
            }
        } catch (final NoResultException nrEx) {
            LOG.debug("Message with id [" + messageId + "] was not found", nrEx);
            throw new MessageNotFoundException("Message with id [" + messageId + "] was not found");
        }

        validateOriginalUser(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);

        userMessageLogDao.setMessageAsDownloaded(messageId);
        // Deleting the message and signal message if the retention download is zero and the payload is not stored on the file system.
        if (0 == pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc()) && !userMessage.isPayloadOnFileSystem()) {
            messagingDao.clearPayloadData(messageId);
            List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(messageId);
            if (!signalMessages.isEmpty()) {
                for (SignalMessage signalMessage : signalMessages) {
                    signalMessageDao.clear(signalMessage);
                }
            }
            // Sets the message log status to DELETED
            userMessageLogDao.setMessageAsDeleted(messageId);
            // Sets the log status to deleted also for the signal messages (if present).
            List<String> signalMessageIds = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
            if (!signalMessageIds.isEmpty()) {
                for (String signalMessageId : signalMessageIds) {
                    signalMessageLogDao.setMessageAsDeleted(signalMessageId);
                }
            }
        }
        LOG.info("transformer.transformFromMessaging UserMessage = " + userMessage.toString());
        return transformer.transformFromMessaging(userMessage);
    }

    private void validateOriginalUser(UserMessage userMessage, String authOriginalUser, String type) {
        if (authOriginalUser != null) {
            LOG.debug("OriginalUser is " + authOriginalUser);
            /* check the message belongs to the authenticated user */
            String originalUser = getOriginalUser(userMessage, type);
            if (originalUser != null && !originalUser.equals(authOriginalUser)) {
                LOG.debug("User:" + authOriginalUser + " is trying to delete message having finalRecipient:" + originalUser);
                throw new AccessDeniedException("You are not allowed to handle this message. You are authorized as " + authOriginalUser);
            }
        }
    }

    private String getOriginalUser(UserMessage userMessage, String type) {
        if (userMessage == null || userMessage.getMessageProperties() == null ||
                userMessage.getMessageProperties().getProperty() == null) {
            return null;
        }
        String originalUser = null;
        for (Property property : userMessage.getMessageProperties().getProperty()) {
            if (property.getName() != null && property.getName().equals(type)) {
                originalUser = property.getValue();
                break;
            }
        }
        return originalUser;
    }

    @Override
    public MessageStatus getMessageStatus(final String messageId) {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasAdminRole();

        return userMessageLogDao.getMessageStatus(messageId);
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasAdminRole();

        return errorLogDao.getErrorsForMessage(messageId);
    }


    @Override
    @Transactional
    public String submit(final Submission messageData, final String backendName) throws MessagingProcessingException {

        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasUserOrAdminRole();
        String originalUser = authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
        LOG.debug("Authorized as " + (originalUser == null ? "super user" : originalUser));

        try {
            final UserMessage userMessage = transformer.transformFromSubmission(messageData);
            final MessageInfo messageInfo = userMessage.getMessageInfo();
            if (messageInfo == null) {
                userMessage.setMessageInfo(objectFactory.createMessageInfo());
            }
            String messageId = userMessage.getMessageInfo().getMessageId();
            if (messageId == null || userMessage.getMessageInfo().getMessageId().trim().isEmpty()) {
                messageId = messageIdGenerator.generateMessageId();
                userMessage.getMessageInfo().setMessageId(messageId);
            } else if (messageId.length() > 255) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", messageId, null);
            }
            String refToMessageId = userMessage.getMessageInfo().getRefToMessageId();
            if (refToMessageId != null && refToMessageId.length() > 255) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "RefToMessageId value is too long (over 255 characters)", refToMessageId, null);
            }
            // handle if the messageId is unique. This should only fail if the ID is set from the outside
            if (!MessageStatus.NOT_FOUND.equals(userMessageLogDao.getMessageStatus(messageId))) {
                throw new DuplicateMessageException("Message with id [" + messageId + "] already exists. Message identifiers must be unique");
            }

            final String pmodeKey;
            final Messaging message = ebMS3Of.createMessaging();
            message.setUserMessage(userMessage);

            validateOriginalUser(userMessage, originalUser, MessageConstants.ORIGINAL_SENDER);

            try {
                pmodeKey = pModeProvider.findPModeKeyForUserMessage(userMessage, MSHRole.SENDING);
            } catch (IllegalStateException e) { //if no pmodes are configured
                throw new PModeMismatchException("PMode could not be found. Are PModes configured in the database?");
            }

            final Party from = pModeProvider.getSenderParty(pmodeKey);
            final Party to = pModeProvider.getReceiverParty(pmodeKey);
            // Verifies that the initiator and responder party are not the same.
            if (from.getName().equals(to.getName())) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's name is the same as the responder party's one[" + from.getName() + "]", null, null);
            }
            // Verifies that the message is not for the current gateway.
            Configuration config = pModeProvider.getConfigurationDAO().read();
            if (config.getParty().equals(to)) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "It is forbidden to submit a message to the sending access point[" + to.getName() + "]", null, null);
            }

            final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pmodeKey);
            final Map<Party, Mpc> mpcMap = legConfiguration.getPartyMpcMap();
            String mpc = Mpc.DEFAULT_MPC;
            if (legConfiguration.getDefaultMpc() != null) {
                mpc = legConfiguration.getDefaultMpc().getQualifiedName();
            }
            if (mpcMap != null && mpcMap.containsKey(to)) {
                mpc = mpcMap.get(to).getQualifiedName();
            }
            userMessage.setMpc(mpc);
            payloadProfileValidator.validate(message, pmodeKey);
            propertyProfileValidator.validate(message, pmodeKey);
            int sendAttemptsMax = 1;

            if (legConfiguration.getReceptionAwareness() != null) {
                sendAttemptsMax = legConfiguration.getReceptionAwareness().getRetryCount();
            }

            try {
                final boolean compressed = compressionService.handleCompression(userMessage, legConfiguration);
                LOG.debug("Compression for message with id: " + userMessage.getMessageInfo().getMessageId() + " applied: " + compressed);
            } catch (final EbMS3Exception e) {
                errorLogDao.create(new ErrorLogEntry(e));
                throw e;
            }

            // We do not create MessageIds for SignalMessages, as those should never be submitted via the backend
            messagingDao.create(message);
            // TODO Should we store the user message log before it is dispatched to the queue ?
            // Sends message to the proper queue
            jmsManager.sendMessageToQueue(new DispatchMessageCreator(messageId, to.getEndpoint()).createMessage(), sendMessageQueue);
            // Builds the user message log
            UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                    .setMessageId(userMessage.getMessageInfo().getMessageId())
                    .setMessageStatus(MessageStatus.SEND_ENQUEUED)
                    .setMshRole(MSHRole.SENDING)
                    .setNotificationStatus(legConfiguration.getErrorHandling().isBusinessErrorNotifyProducer() ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED)
                    .setMpc(message.getUserMessage().getMpc())
                    .setSendAttemptsMax(sendAttemptsMax)
                    .setBackendName(backendName)
                    .setEndpoint(to.getEndpoint());

            userMessageLogDao.create(umlBuilder.build());

            return userMessage.getMessageInfo().getMessageId();

        } catch (final EbMS3Exception e) {
            LOG.error("Error submitting to backendName :" + backendName, e);
            //TODO revise the way how we handle the exceptions here; the exception factory below should be removed
            throw MessagingExceptionFactory.transform(e);
        }
    }

}
