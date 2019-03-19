package eu.domibus.ebms3.sender;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;

/**
 * Listeners that deletes messages by their identifiers and clears any signal message related to them.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */
@Service
public class RetentionListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionListener.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        try {
            String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            LOG.debug("Processing retention message [{}]", messageId);

            messagingDao.clearPayloadData(messageId);
            userMessageLogService.setMessageAsDeleted(messageId);
            handleSignalMessageDelete(messageId);
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
    }

    protected void handleSignalMessageDelete(String messageId) {
        List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(messageId);
        if (!signalMessages.isEmpty()) {
            for (SignalMessage signalMessage : signalMessages) {
                signalMessageDao.clear(signalMessage);
            }
        }
        List<String> signalMessageIds = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
        if (!signalMessageIds.isEmpty()) {
            for (String signalMessageId : signalMessageIds) {
                userMessageLogService.setMessageAsDeleted(signalMessageId);
            }
        }
    }
}
