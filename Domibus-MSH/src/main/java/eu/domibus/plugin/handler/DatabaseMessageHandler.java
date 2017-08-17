package eu.domibus.plugin.handler;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.*;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.exception.MessagingExceptionFactory;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.validators.BackendMessageValidator;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.*;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandler.class);

    private final ObjectFactory ebMS3Of = new ObjectFactory();

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private SubmissionAS4Transformer transformer;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogService userMessageLogService;

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
    private BackendMessageValidator backendMessageValidator;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    UserMessageService userMessageService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasUserOrAdminRole();

        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        LOG.debug("Authorized as " + (originalUser == null ? "super user" : originalUser));

        UserMessage userMessage;
        try {
            LOG.info("Searching message with id [" + messageId + "]");
            userMessage = messagingDao.findUserMessageByMessageId(messageId);
            // Authorization check
            validateOriginalUser(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);

            UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.RECEIVING);
            if (userMessageLog == null) {
                throw new MessageNotFoundException("Message with id [" + messageId + "] was not found");
            }
        } catch (final NoResultException nrEx) {
            LOG.debug("Message with id [" + messageId + "] was not found", nrEx);
            throw new MessageNotFoundException("Message with id [" + messageId + "] was not found");
        }

        userMessageLogService.setMessageAsDownloaded(messageId);
        // Deleting the message and signal message if the retention download is zero and the payload is not stored on the file system.
        if (userMessage != null && 0 == pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc()) && !userMessage.isPayloadOnFileSystem()) {
            messagingDao.clearPayloadData(messageId);
            List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(messageId);
            if (!signalMessages.isEmpty()) {
                for (SignalMessage signalMessage : signalMessages) {
                    signalMessageDao.clear(signalMessage);
                }
            }
            // Sets the message log status to DELETED
            userMessageLogService.setMessageAsDeleted(messageId);
            // Sets the log status to deleted also for the signal messages (if present).
            List<String> signalMessageIds = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
            if (!signalMessageIds.isEmpty()) {
                for (String signalMessageId : signalMessageIds) {
                    userMessageLogService.setMessageAsDeleted(signalMessageId);
                }
            }
        }
        return transformer.transformFromMessaging(userMessage);
    }

    private void validateOriginalUser(UserMessage userMessage, String authOriginalUser, String recipient) {
        if (authOriginalUser != null) {
            LOG.debug("OriginalUser is [" + authOriginalUser + "]");
            /* check the message belongs to the authenticated user */
            String originalUser = getOriginalUser(userMessage, recipient);
            if (originalUser != null && !originalUser.equals(authOriginalUser)) {
                LOG.debug("User [" + authOriginalUser + "] is trying to submit/access a message having as final recipient: " + originalUser);
                throw new AccessDeniedException("You are not allowed to handle this message. You are authorized as [" + authOriginalUser + "]");
            }
        }
    }

    private String getOriginalUser(UserMessage userMessage, String type) {
        if (userMessage == null || userMessage.getMessageProperties() == null || userMessage.getMessageProperties().getProperty() == null) {
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

        return convertMessageStatus(userMessageLogDao.getMessageStatus(messageId));
    }

    protected MessageStatus convertMessageStatus(MessageStatus messageStatus) {
        if (MessageStatus.DOWNLOADED == messageStatus) {
            LOG.warn("Using deprecated method that converts DOWNLOADED status to RECEIVED");
            //convert the DOWNLOADED status to RECEIVED to assure backwards compatibility
            messageStatus = eu.domibus.common.MessageStatus.RECEIVED;
        }
        return messageStatus;
    }

    @Override
    public MessageStatus getStatus(final String messageId) {
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
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public String submit(final Submission messageData, final String backendName) throws MessagingProcessingException {
        if (StringUtils.isNotEmpty(messageData.getMessageId())) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageData.getMessageId());
        }
        LOG.info("Preparing to submit message");
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }

        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        LOG.debug("Authorized as " + (originalUser == null ? "super user" : originalUser));

        UserMessage userMessage = transformer.transformFromSubmission(messageData);

        if(userMessage == null) {
            LOG.warn("UserMessage is null");
            throw new MessageNotFoundException("UserMessage is null");
        }

        validateOriginalUser(userMessage, originalUser, MessageConstants.ORIGINAL_SENDER);

        try {
            // MessageInfo is always initialized in the get method
            MessageInfo messageInfo = userMessage.getMessageInfo();
            String messageId = messageInfo.getMessageId();
            if (messageId == null) {
                messageId = messageIdGenerator.generateMessageId();
                messageInfo.setMessageId(messageId);
            } else {
                backendMessageValidator.validateMessageId(messageId);
                userMessage.getMessageInfo().setMessageId(messageId);
            }
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageInfo.getMessageId());

            String refToMessageId = messageInfo.getRefToMessageId();
            if (refToMessageId != null) {
                backendMessageValidator.validateRefToMessageId(refToMessageId);
            }
            // handle if the messageId is unique. This should only fail if the ID is set from the outside
            if (!MessageStatus.NOT_FOUND.equals(userMessageLogDao.getMessageStatus(messageId))) {
                throw new DuplicateMessageException("Message with id [" + messageId + "] already exists. Message identifiers must be unique");
            }

            Messaging message = ebMS3Of.createMessaging();
            message.setUserMessage(userMessage);

            MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();
            Party to = messageValidations(userMessage, pModeKey, backendName);

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);

            fillMpc(userMessage, legConfiguration, to);

            payloadProfileValidator.validate(message, pModeKey);
            propertyProfileValidator.validate(message, pModeKey);

            boolean compressed = compressionService.handleCompression(userMessage, legConfiguration);
            LOG.debug("Compression for message with id: " + messageId + " applied: " + compressed);

            try {
                messagingService.storeMessage(message);
            } catch (CompressionException exc) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, userMessage.getMessageInfo().getMessageId());
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }
            MessageStatus messageStatus = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
            if (MessageStatus.READY_TO_PULL != messageStatus) {
                // Sends message to the proper queue if not a message to be pulled.
                userMessageService.scheduleSending(messageId);
            }

            userMessageLogService.save(messageId, messageStatus.toString(), getNotificationStatus(legConfiguration).toString(), MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), message.getUserMessage().getMpc(), backendName, to.getEndpoint());

            LOG.info("Message submitted");
            return userMessage.getMessageInfo().getMessageId();

        } catch (EbMS3Exception ebms3Ex) {
            LOG.error("Error submitting the message [" + userMessage.getMessageInfo().getMessageId() + "] to [" + backendName + "]", ebms3Ex);
            errorLogDao.create(new ErrorLogEntry(ebms3Ex));
            throw MessagingExceptionFactory.transform(ebms3Ex);
        } catch (PModeException p) {
            LOG.error("Error submitting the message [" + userMessage.getMessageInfo().getMessageId() + "] to [" + backendName + "]" + p.getMessage());
            errorLogDao.create(new ErrorLogEntry(MSHRole.SENDING, userMessage.getMessageInfo().getMessageId(), ErrorCode.EBMS_0010, p.getMessage()));
            throw new PModeMismatchException(p.getMessage(), p);
        }

    }

    private Party messageValidations(UserMessage userMessage, String pModeKey, String backendName) throws EbMS3Exception, MessagingProcessingException {
        try {
            Party from = pModeProvider.getSenderParty(pModeKey);
            Party to = pModeProvider.getReceiverParty(pModeKey);
            backendMessageValidator.validateParties(from, to);

            Party gatewayParty = pModeProvider.getGatewayParty();
            backendMessageValidator.validateInitiatorParty(gatewayParty, from);
            backendMessageValidator.validateResponderParty(gatewayParty, to);

            return to;
        } catch (IllegalArgumentException runTimEx) {
            LOG.error("Error submitting the message [" + userMessage.getMessageInfo().getMessageId() + "] to [" + backendName + "]", runTimEx);
            throw MessagingExceptionFactory.transform(runTimEx, ErrorCode.EBMS_0003);
        }
    }

    private NotificationStatus getNotificationStatus(LegConfiguration legConfiguration) {
        return legConfiguration.getErrorHandling().isBusinessErrorNotifyProducer() ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED;
    }

    private int getMaxAttempts(LegConfiguration legConfiguration) {
        return ( legConfiguration.getReceptionAwareness() == null ? 1 : legConfiguration.getReceptionAwareness().getRetryCount() ) + 1; // counting retries after the first send attempt
    }

    private void fillMpc(UserMessage userMessage, LegConfiguration legConfiguration, Party to) {
        final Map<Party, Mpc> mpcMap = legConfiguration.getPartyMpcMap();
        String mpc = Ebms3Constants.DEFAULT_MPC;
        if (legConfiguration.getDefaultMpc() != null) {
            mpc = legConfiguration.getDefaultMpc().getQualifiedName();
        }
        if (mpcMap != null && mpcMap.containsKey(to)) {
            mpc = mpcMap.get(to).getQualifiedName();
        }
        userMessage.setMpc(mpc);
    }

}
