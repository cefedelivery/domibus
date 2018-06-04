package eu.domibus.ebms3.sender;

import eu.domibus.api.jms.DomibusJMSException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.core.pull.MessagingLockDao;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class RetryService {
    public static final String TIMEOUT_TOLERANCE = "domibus.msh.retry.tolerance";
    private static final String DELETE_PAYLOAD_ON_SEND_FAILURE = "domibus.sendMessage.failure.delete.payload";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetryService.class);
    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Autowired
    @Qualifier("jmsTemplateDispatch")
    private JmsOperations jmsOperations;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue dispatchQueue;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private MessagingLockDao messagingLockDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueMessages() {
        final List<String> messageIdsToPurge = userMessageLogDao.findTimedoutMessages(Integer.parseInt(domibusProperties.getProperty(RetryService.TIMEOUT_TOLERANCE)));
        for (final String messageIdToPurge : messageIdsToPurge) {
            purgeTimedoutMessage(messageIdToPurge);
        }
        LOG.trace(messageIdsToPurge.size() + " messages to purge found");

        final List<String> messagesNotAlreadyQueued = getMessagesNotAlreadyQueued();
        for (final String messageId : messagesNotAlreadyQueued) {
            userMessageService.scheduleSending(messageId);
        }
    }

    protected List<String> getMessagesNotAlreadyQueued() {
        List<String> result = new ArrayList<>();

        final List<String> messageIdsToSend = userMessageLogDao.findRetryMessages();
        if (messageIdsToSend.isEmpty()) {
            return result;
        }
        LOG.debug("Messages to be retried [{}]", messageIdsToSend);
        final List<String> queuedMessages = getQueuedMessages();
        messageIdsToSend.removeAll(queuedMessages);
        return messageIdsToSend;
    }

    protected List<String> getQueuedMessages() {
        List<String> result = new ArrayList<>();
        try {
            final List<JmsMessage> jmsMessages = jmsManager.browseMessages(dispatchQueue.getQueueName());
            if (jmsMessages == null) {
                return result;
            }
            for (JmsMessage jmsMessage : jmsMessages) {
                result.add(jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID));
            }
            return result;
        } catch (JMSException e) {
            throw new DomibusJMSException(e);
        }
    }


    /**
     * Notifies send failure, updates the message status and deletes the payload (if required) for messages that failed to be sent and expired
     *
     * @param messageIdToPurge is the messageId of the expired message
     */
    //TODO in Domibus 3.3 extract the logic below into a method of the MessageService and re-use it here and in the UpdateRetryLoggingService
    private void purgeTimedoutMessage(final String messageIdToPurge) {
        final MessageLog userMessageLog = userMessageLogDao.findByMessageId(messageIdToPurge, MSHRole.SENDING);

        final boolean notify = NotificationStatus.REQUIRED.equals(userMessageLog.getNotificationStatus());

        if (notify) {
            backendNotificationService.notifyOfSendFailure(messageIdToPurge);

        }
        userMessageLogService.setMessageAsSendFailure(messageIdToPurge);

        if ("true".equals(domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false"))) {
            messagingDao.clearPayloadData(messageIdToPurge);
        }
    }

    /**
     * Notifies send failure, updates the message status and deletes the payload (if required) for messages that failed to be sent and expired
     * Note: This method creates a new transaction
     *
     * @param messageIdToPurge is the messageId of the expired message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purgeTimedoutMessageInANewTransaction(final String messageIdToPurge) {
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageIdToPurge);
        purgeTimedoutMessage(messageIdToPurge);
    }

    /**
     * Method call by job to reset waiting_for_receipt messages into ready to pull.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetWaitingForReceiptPullMessages() {
        final List<MessagingLock> messagesToReset = messagingLockDao.findWaitingForReceipt();
        for (MessagingLock messagingLock : messagesToReset) {
            pullMessageService.resetMessageInWaitingForReceiptState(messagingLock.getMessageId());
        }
    }


    /**
     * Method call by job to to expire messages that could not be delivered in the configured time range..
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkExpirePullMessages() {
        final List<MessagingLock> expiredMessages = messagingLockDao.findStaledMessages();
        LOG.trace("Delete expired pull message");
        for (MessagingLock staledMessage : expiredMessages) {
            pullMessageService.expireMessage(staledMessage.getMessageId());
        }
    }

    /**
     * Method call by job to to delete messages marked as failed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkDeletePullMessages() {
        final List<MessagingLock> deletedLocks = messagingLockDao.findDeletedMessages();
        LOG.trace("Delete unecessary locks");
        for (MessagingLock deletedLock : deletedLocks) {
            pullMessageService.deleteInNewTransaction(deletedLock.getMessageId());
        }
    }


}
