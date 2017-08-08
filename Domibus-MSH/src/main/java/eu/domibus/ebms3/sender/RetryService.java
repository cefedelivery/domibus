package eu.domibus.ebms3.sender;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.message.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.Enumeration;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueMessages() {
        final List<String> messageIdsToPurge = userMessageLogDao.findTimedoutMessages(Integer.parseInt(domibusProperties.getProperty(RetryService.TIMEOUT_TOLERANCE)));
        for (final String messageIdToPurge : messageIdsToPurge) {
            purgeTimedoutMessage(messageIdToPurge);
        }
        LOG.debug(messageIdsToPurge.size() + " messages to purge found");
        final List<String> messageIdsToSend = userMessageLogDao.findRetryMessages();
        if (!messageIdsToSend.isEmpty()) {
            jmsOperations.browse(dispatchQueue, new BrowserCallback<Void>() {
                @Override
                public Void doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                    final Enumeration browserEnumeration = browser.getEnumeration();
                    while (browserEnumeration.hasMoreElements()) {
                        messageIdsToSend.remove(((Message) browserEnumeration.nextElement()).getStringProperty(MessageConstants.MESSAGE_ID));
                    }
                    return null;
                }
            });
            for (final String messageId : messageIdsToSend) {
                userMessageService.scheduleSending(messageId);
            }
        }
        resetUnAcknowledgedPullMessage();
    }

    //@thom test this
    private void resetUnAcknowledgedPullMessage(){
        List<String> timedoutPullMessages = userMessageLogDao.findTimedoutPullMessages(Integer.parseInt(domibusProperties.getProperty(RetryService.TIMEOUT_TOLERANCE)));
        for (final String timedoutPullMessage : timedoutPullMessages) {
            purgeTimedoutMessage(timedoutPullMessage);
        }
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
            userMessageLogDao.setAsNotified(messageIdToPurge);
        }
        userMessageLogService.setMessageAsSendFailure(messageIdToPurge);

        if ("true".equals(domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false"))) {
            messagingDao.clearPayloadData(messageIdToPurge);
        }
    }

}
