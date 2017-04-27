package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.UserMessageServiceHelper;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.DelayedDispatchMessageCreator;
import eu.domibus.messaging.DispatchMessageCreator;
import eu.domibus.plugin.NotificationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Queue;
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
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

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

        userMessageLog.setMessageStatus(MessageStatus.SEND_ENQUEUED);
        final Date currentDate = new Date();
        userMessageLog.setRestored(currentDate);
        userMessageLog.setFailed(null);
        userMessageLog.setNextAttempt(currentDate);

        final LegConfiguration legConfiguration = pModeService.getLegConfiguration(messageId);
        Integer maxAttemptsConfiguration = 1;
        if (legConfiguration == null) {
            LOG.warn("Could not get the leg configuration for message [{}]. Using the default maxAttempts configuration [{}]", messageId, maxAttemptsConfiguration);
        } else {
            maxAttemptsConfiguration = pModeServiceHelper.getMaxAttempts(legConfiguration);
        }
        Integer newMaxAttempts = userMessageLog.getSendAttemptsMax() + maxAttemptsConfiguration;
        LOG.debug("Increasing the max attempts for message [{}] from [{}] to [{}]", messageId, userMessageLog.getSendAttemptsMax(), newMaxAttempts);
        userMessageLog.setSendAttemptsMax(newMaxAttempts);

        userMessageLogDao.update(userMessageLog);

        scheduleSending(messageId);
    }

    @Override
    public void scheduleSending(String messageId) {
        jmsManager.sendMessageToQueue(new DispatchMessageCreator(messageId).createMessage(), sendMessageQueue);
    }

    @Override
    public void scheduleSending(String messageId, Long delay) {
        jmsManager.sendMessageToQueue(new DelayedDispatchMessageCreator(messageId, delay).createMessage(), sendMessageQueue);
    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end, String finalRecipient) {
        return null;
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
    public void deleteMessage(String messageId) {
        LOG.debug("Deleting message [{}]", messageId);
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
        userMessageLogDao.setMessageAsDeleted(messageId);
        handleSignalMessageDelete(messageId);
    }

    private void handleSignalMessageDelete(String messageId) {
        List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(messageId);
        if (!signalMessages.isEmpty()) {
            for (SignalMessage signalMessage : signalMessages) {
                signalMessageDao.clear(signalMessage);
            }
        }
        List<String> signalMessageIds = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
        if (!signalMessageIds.isEmpty()) {
            for (String signalMessageId : signalMessageIds) {
                signalMessageLogDao.setMessageAsDeleted(signalMessageId);
            }
        }
    }
}
