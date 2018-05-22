package eu.domibus.ebms3.sender;

import eu.domibus.api.jms.DomibusJMSException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.pull.MessagingLockService;
import eu.domibus.core.pull.ToExtractor;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.To;
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
    protected DomibusPropertyProvider domibusPropertyProvider;

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
    private MessagingLockService messagingLockService;

    @Autowired
    private JMSManager jmsManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueMessages() {
        final List<String> messageIdsToPurge = userMessageLogDao.findTimedoutMessages(Integer.parseInt(domibusPropertyProvider.getProperty(RetryService.TIMEOUT_TOLERANCE)));
        for (final String messageIdToPurge : messageIdsToPurge) {
            purgeTimedoutMessage(messageIdToPurge);
        }
        LOG.debug(messageIdsToPurge.size() + " messages to purge found");

        final List<String> messagesNotAlreadyQueued = getMessagesNotAlreadyQueued();
        for (final String messageId : messagesNotAlreadyQueued) {
            userMessageService.scheduleSending(messageId);
        }

        purgePullMessage();
        resetWaitingForReceiptPullMessages();
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

    //@thom test this
    protected void purgePullMessage() {
        List<String> timedoutPullMessages = userMessageLogDao.findTimedOutPullMessages(Integer.parseInt(domibusPropertyProvider.getProperty(RetryService.TIMEOUT_TOLERANCE)));
        for (final String timedoutPullMessage : timedoutPullMessages) {
            messagingLockService.delete(timedoutPullMessage);
            purgeTimedoutMessage(timedoutPullMessage);
        }
    }

    protected void resetWaitingForReceiptPullMessages() {
        final List<String> messagesToReset = userMessageLogDao.findPullWaitingForReceiptMessages();
        for (String messagedId : messagesToReset) {
            final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messagedId);
            if (userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Message " + messagedId + " set back in READY_TO_PULL state.");
                }
                addPullMessageSearchInformation(messagedId);
                userMessageLog.setMessageStatus(MessageStatus.READY_TO_PULL);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Pull Message with " + messagedId + " marked as send failure after max retry attempt reached");
                }
                userMessageLog.setMessageStatus(MessageStatus.SEND_FAILURE);
                messagingLockService.delete(messagedId);
            }
            userMessageLogDao.update(userMessageLog);
        }
    }

    /**
     * When a message has been set in waiting_for_receipt state its locking record has been deleted. When the retry
     * service set timed_out waiting_for_receipt messages back in ready_to_pull state, the search and lock system has to be fed again
     * with the message information.
     * @param messagedId the id of the message to reset
     */
    private void addPullMessageSearchInformation(final String messagedId) {
        Messaging messageByMessageId = messagingDao.findMessageByMessageId(messagedId);
        To to = messageByMessageId.getUserMessage().getPartyInfo().getTo();
        messagingLockService.delete(messagedId);
        messagingLockService.addSearchInFormation(new ToExtractor(to),messagedId,messageByMessageId.getUserMessage().getMpc());
    }


    /**
     * Notifies send failure, updates the message status and deletes the payload (if required) for messages that failed to be sent and expired
     *
     * @param messageIdToPurge is the messageId of the expired message
     */
    //TODO in Domibus 3.3 extract the logic below into a method of the MessageService and re-use it here and in the UpdateRetryLoggingService
    public void purgeTimedoutMessage(final String messageIdToPurge) {
        final MessageLog userMessageLog = userMessageLogDao.findByMessageId(messageIdToPurge, MSHRole.SENDING);

        final boolean notify = NotificationStatus.REQUIRED.equals(userMessageLog.getNotificationStatus());

        if (notify) {
            backendNotificationService.notifyOfSendFailure(messageIdToPurge);

        }
        userMessageLogService.setMessageAsSendFailure(messageIdToPurge);

        if ("true".equals(domibusPropertyProvider.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false"))) {
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
        purgeTimedoutMessage(messageIdToPurge);
    }

}
