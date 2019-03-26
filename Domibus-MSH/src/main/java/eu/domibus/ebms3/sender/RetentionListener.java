package eu.domibus.ebms3.sender;

import eu.domibus.core.message.UserMessageDefaultService;
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

/**
 * Listeners that deletes messages by their identifiers.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */
@Service
public class RetentionListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionListener.class);

    @Autowired
    private UserMessageDefaultService userMessageDefaultService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        try {
            String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            LOG.debug("Processing retention message [{}]", messageId);

            userMessageDefaultService.deleteMessage(messageId);
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
    }
}
