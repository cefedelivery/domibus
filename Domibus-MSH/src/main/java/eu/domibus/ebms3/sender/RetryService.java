package eu.domibus.ebms3.sender;

import eu.domibus.api.jms.DomibusJMSException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.UserMessageLogEntity;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.pull.MessagingLockDao;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetryService.class);

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

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

    @Autowired
    PModeProvider pModeProvider;

    @Autowired
    UpdateRetryLoggingService updateRetryLoggingService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueMessages() {
        final List<String> messagesNotAlreadyQueued = getMessagesNotAlreadyQueued();
        for (final String messageId : messagesNotAlreadyQueued) {
            if(!failIfExpired(messageId)) {
                userMessageService.scheduleSending(messageId);
            }
        }
    }

    protected boolean failIfExpired(String messageId) {
        UserMessageLogEntity userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        eu.domibus.common.model.configuration.LegConfiguration legConfiguration = null;
        final String pModeKey;

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        try {
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
        } catch (EbMS3Exception exc) {
            LOG.warn("Could not find LegConfiguration for message [{}]", messageId);
            return false;
        }
        if(updateRetryLoggingService.isExpired(legConfiguration, userMessageLog)) {
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
            return true;
        }
        return false;
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
            final List<JmsMessage> jmsMessages = jmsManager.browseClusterMessages(dispatchQueue.getQueueName());
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
