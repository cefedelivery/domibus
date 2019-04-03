package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.core.message.fragment.MessageGroupDao;
import eu.domibus.core.message.fragment.MessageGroupEntity;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.core.pull.ToExtractor;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.UserMessageServiceHelper;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.SplitAndJoinException;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.DelayedDispatchMessageCreator;
import eu.domibus.messaging.DispatchMessageCreator;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.handler.DatabaseMessageHandler;
import eu.domibus.plugin.transformer.impl.UserMessageFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageDefaultService implements UserMessageService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultService.class);

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue sendMessageQueue;

    @Autowired
    @Qualifier("sendLargeMessageQueue")
    private Queue sendLargeMessageQueue;

    @Autowired
    @Qualifier("splitAndJoinQueue")
    private Queue splitAndJoinQueue;

    @Autowired
    @Qualifier("sendPullReceiptQueue")
    private Queue sendPullReceiptQueue;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    PModeService pModeService;

    @Autowired
    PModeServiceHelper pModeServiceHelper;

    @Autowired
    private MessageExchangeService messageExchangeService;

    //TODO remove the ext converter and replace it with DomainCoreConverter
    @Autowired
    private DomainExtConverter domainExtConverter;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    protected DatabaseMessageHandler databaseMessageHandler;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    public void createMessageFragments(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, List<String> fragmentFiles) {
        messageGroupDao.create(messageGroupEntity);

        String backendName = userMessageLogDao.findBackendForMessageId(sourceMessage.getMessageInfo().getMessageId());
        for (int index = 0; index < fragmentFiles.size(); index++) {
            try {
                final String fragmentFile = fragmentFiles.get(index);
                createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragmentFile, index + 1);
            } catch (MessagingProcessingException e) {
                throw new SplitAndJoinException("Could not create Messaging for fragment " + index, e);
            }
        }
    }

    protected void createMessagingForFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, String backendName, String fragmentFile, int index) throws MessagingProcessingException {
        final UserMessage userMessageFragment = userMessageFactory.createUserMessageFragment(userMessage, messageGroupEntity, Long.valueOf(index), fragmentFile);
        databaseMessageHandler.submitMessageFragment(userMessageFragment, backendName);
    }

    @Override
    public String getFinalRecipient(String messageId) {
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            LOG.debug("Message [{}] does not exist", messageId);
            return null;
        }
        return userMessageServiceHelper.getFinalRecipient(userMessage);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient) {
        LOG.debug("Provided finalRecipient is [{}]", finalRecipient);
        return userMessageLogDao.findFailedMessages(finalRecipient);
    }

    @Override
    public Long getFailedMessageElapsedTime(String messageId) {
        final UserMessageLog userMessageLog = getFailedMessage(messageId);
        final Date failedDate = userMessageLog.getFailed();
        if (failedDate == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not compute failed elapsed time for message [" + messageId + "]: failed date is empty");
        }
        return System.currentTimeMillis() - failedDate.getTime();

    }

    @Override
    public void restoreFailedMessage(String messageId) {
        LOG.info("Restoring message [{}]", messageId);
        final UserMessageLog userMessageLog = getFailedMessage(messageId);

        if (MessageStatus.DELETED == userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not restore message [" + messageId + "]. Message status is [" + MessageStatus.DELETED + "]");
        }

        final MessageStatus newMessageStatus = messageExchangeService.retrieveMessageRestoreStatus(messageId);
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, newMessageStatus, new Timestamp(System.currentTimeMillis()));
        userMessageLog.setMessageStatus(newMessageStatus);
        final Date currentDate = new Date();
        userMessageLog.setRestored(currentDate);
        userMessageLog.setFailed(null);
        userMessageLog.setNextAttempt(currentDate);

        Integer newMaxAttempts = computeNewMaxAttempts(userMessageLog, messageId);
        LOG.debug("Increasing the max attempts for message [{}] from [{}] to [{}]", messageId, userMessageLog.getSendAttemptsMax(), newMaxAttempts);
        userMessageLog.setSendAttemptsMax(newMaxAttempts);

        userMessageLogDao.update(userMessageLog);
        uiReplicationSignalService.messageChange(userMessageLog.getMessageId());

        if (MessageStatus.READY_TO_PULL != newMessageStatus) {
            scheduleSending(messageId);
        } else {
            final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
            LOG.debug("[restoreFailedMessage]:Message:[{}] add lock", userMessageLog.getMessageId());
            pullMessageService.addPullMessageLock(new ToExtractor(userMessage.getPartyInfo().getTo()), userMessage, userMessageLog);
        }
    }

    @Override
    public void sendEnqueuedMessage(String messageId) {
        LOG.info("Sending enqueued message [{}]", messageId);

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (userMessageLog == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Message [" + messageId + "] does not exist");
        }
        if (MessageStatus.SEND_ENQUEUED != userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Message [" + messageId + "] status is not [" + MessageStatus.SEND_ENQUEUED + "]");
        }

        userMessageLog.setNextAttempt(new Date());
        userMessageLogDao.update(userMessageLog);
        scheduleSending(messageId);
    }

    @Override
    public void resendFailedOrSendEnqueuedMessage(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (userMessageLog == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Message [" + messageId + "] does not exist");
        }
        if (MessageStatus.SEND_ENQUEUED == userMessageLog.getMessageStatus()) {
            sendEnqueuedMessage(messageId);
        } else {
            restoreFailedMessage(messageId);
        }
    }

    protected Integer getMaxAttemptsConfiguration(final String messageId) {
        final LegConfiguration legConfiguration = pModeService.getLegConfiguration(messageId);
        Integer result = 1;
        if (legConfiguration == null) {
            LOG.warn("Could not get the leg configuration for message [{}]. Using the default maxAttempts configuration [{}]", messageId, result);
        } else {
            result = pModeServiceHelper.getMaxAttempts(legConfiguration);
        }
        return result;
    }

    protected Integer computeNewMaxAttempts(final UserMessageLog userMessageLog, final String messageId) {
        Integer maxAttemptsConfiguration = getMaxAttemptsConfiguration(messageId);
        // always increase maxAttempts (even when not reached by sendAttempts)
        return userMessageLog.getSendAttemptsMax() + maxAttemptsConfiguration + 1; // max retries plus initial reattempt
    }

    @Override
    public void scheduleSending(String messageId, int retryCount) {
        scheduleSending(messageId, new DispatchMessageCreator(messageId).createMessage(retryCount));
    }

    @Override
    public void scheduleSending(String messageId) {
        scheduleSending(messageId, new DispatchMessageCreator(messageId).createMessage());
    }

    @Override
    public void scheduleSending(String messageId, Long delay) {
        scheduleSending(messageId, new DelayedDispatchMessageCreator(messageId, delay).createMessage());
    }

    @Override
    public void scheduleSourceMessageSending(String messageId) {
        LOG.debug("Sending message to sendLargeMessageQueue");
        final JmsMessage jmsMessage = new DispatchMessageCreator(messageId).createMessage();
        jmsManager.sendMessageToQueue(jmsMessage, sendLargeMessageQueue);
    }

    protected void scheduleSending(String messageId, JmsMessage jmsMessage) {
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        if (userMessage.isSplitAndJoin()) {
            LOG.debug("Sending message to sendLargeMessageQueue");
            jmsManager.sendMessageToQueue(jmsMessage, sendLargeMessageQueue);
        } else {
            LOG.debug("Sending message to sendMessageQueue");
            jmsManager.sendMessageToQueue(jmsMessage, sendMessageQueue);
        }
    }

    @Override
    public void scheduleMessageFragmentSendFailed(String groupId, String backendName) {
        LOG.debug("Scheduling marking the group [{}] as failed", groupId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_MESSAGE_FRAGMENT_SEND_FAILED)
                .property(UserMessageService.MSG_GROUP_ID, groupId)
                .property(UserMessageService.MSG_BACKEND_NAME, backendName)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleUserMessageFragmentFailed(String messageId) {
        final MessageStatus messageStatus = userMessageLogDao.getMessageStatus(messageId);
        if (MessageStatus.SEND_ENQUEUED != messageStatus) {
            LOG.debug("UserMessage fragment [{}] was not scheduled to be marked as failed: status is [{}]", messageId, messageStatus);
            return;
        }

        LOG.debug("Scheduling marking the UserMessage fragment [{}] as failed", messageId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SET_MESSAGE_FRAGMENT_AS_FAILED)
                .property(UserMessageService.MSG_USER_MESSAGE_ID, messageId)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSourceMessageRejoinFile(String groupId, String backendName) {
        LOG.debug("Scheduling the SourceMessage file rejoining for group [{}]", groupId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SOURCE_MESSAGE_REJOIN_FILE)
                .property(UserMessageService.MSG_GROUP_ID, groupId)
                .property(UserMessageService.MSG_BACKEND_NAME, backendName)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSourceMessageRejoin(String groupId, String file, String backendName) {
        LOG.debug("Scheduling the SourceMessage rejoining for group [{}] from file [{}]", groupId, file);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SOURCE_MESSAGE_REJOIN)
                .property(UserMessageService.MSG_GROUP_ID, groupId)
                .property(UserMessageService.MSG_SOURCE_MESSAGE_FILE, file)
                .property(UserMessageService.MSG_BACKEND_NAME, backendName)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSourceMessageReceipt(String messageId, String pmodeKey) {
        LOG.debug("Scheduling the SourceMessage receipt for message [{}]", messageId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SOURCE_MESSAGE_RECEIPT)
                .property(UserMessageService.MSG_SOURCE_MESSAGE_ID, messageId)
                .property(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSendingSignalError(String messageId, String ebMS3ErrorCode, String errorDetail, String pmodeKey) {
        LOG.debug("Scheduling sending the Signal error for message [{}]", messageId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SEND_SIGNAL_ERROR)
                .property(UserMessageService.MSG_USER_MESSAGE_ID, messageId)
                .property(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .property(UserMessageService.MSG_EBMS3_ERROR_CODE, ebMS3ErrorCode)
                .property(UserMessageService.MSG_EBMS3_ERROR_DETAIL, errorDetail)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSendingPullReceipt(String messageId, String pmodeKey) {
        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(PULL_RECEIPT_REF_TO_MESSAGE_ID, messageId)
                .property(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .build();
        LOG.debug("Sending message to sendPullReceiptQueue");
        jmsManager.sendMessageToQueue(jmsMessage, sendPullReceiptQueue);
    }

    @Override
    public eu.domibus.api.usermessage.domain.UserMessage getMessage(String messageId) {
        final UserMessage userMessageByMessageId = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessageByMessageId == null) {
            return null;
        }
        return domainExtConverter.convert(userMessageByMessageId, eu.domibus.api.usermessage.domain.UserMessage.class);
    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date start, Date end, String finalRecipient) {
        final List<String> failedMessages = userMessageLogDao.findFailedMessages(finalRecipient, start, end);
        if (failedMessages == null) {
            return null;
        }
        LOG.debug("Found failed messages [{}] using start date [{}], end date [{}] and final recipient", failedMessages, start, end, finalRecipient);

        final List<String> restoredMessages = new ArrayList<>();
        for (String messageId : failedMessages) {
            try {
                restoreFailedMessage(messageId);
                restoredMessages.add(messageId);
            } catch (Exception e) {
                LOG.error("Failed to restore message [" + messageId + "]", e);
            }
        }

        LOG.debug("Restored messages [{}] using start date [{}], end date [{}] and final recipient", restoredMessages, start, end, finalRecipient);

        return restoredMessages;
    }

    @Override
    public void deleteFailedMessage(String messageId) {
        getFailedMessage(messageId);
        deleteMessage(messageId);
    }

    protected UserMessageLog getFailedMessage(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (userMessageLog == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Message [" + messageId + "] does not exist");
        }
        if (MessageStatus.SEND_FAILURE != userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Message [" + messageId + "] status is not [" + MessageStatus.SEND_FAILURE + "]");
        }
        return userMessageLog;
    }

    @Override
    public void delete(List<String> messageIds) {
        if (messageIds == null) {
            LOG.debug("Nothing to delete");
            return;
        }

        LOG.debug("Deleting [" + messageIds.size() + "] messages");
        for (final String messageId : messageIds) {
            deleteMessage(messageId);
        }
    }

    @Override
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void deleteMessage(String messageId) {
        LOG.debug("Deleting message [{}]", messageId);

        //add messageId to MDC map
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }

        if (backendNotificationService.getNotificationListenerServices() != null) {
            for (NotificationListener notificationListener : backendNotificationService.getNotificationListenerServices()) {
                try {
                    String queueName = notificationListener.getBackendNotificationQueue().getQueueName();
                    JmsMessage message = jmsManager.consumeMessage(queueName, messageId);
                    if (message != null) {
                        LOG.businessInfo(DomibusMessageCode.BUS_MSG_CONSUMED, messageId, queueName);
                    }
                } catch (JMSException jmsEx) {
                    LOG.error("Error trying to get the queue name", jmsEx);
                    throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not get the queue name", jmsEx.getCause());
                }
            }
        }
        messagingDao.clearPayloadData(messageId);
        userMessageLogService.setMessageAsDeleted(messageId);
        handleSignalMessageDelete(messageId);
    }

    protected void handleSignalMessageDelete(String messageId) {
        List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(messageId);
        signalMessages.stream().forEach(signalMessage -> signalMessageDao.clear(signalMessage));

        List<String> signalMessageIds = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
        signalMessageIds.stream().forEach(signalMessageId -> userMessageLogService.setMessageAsDeleted(signalMessageId));
    }
}
