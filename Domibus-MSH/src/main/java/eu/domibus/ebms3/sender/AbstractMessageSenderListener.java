package eu.domibus.ebms3.sender;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public class AbstractMessageSenderListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractMessageSenderListener.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected MessageSenderService messageSenderService;

    @Autowired
    protected UserMessageService userMessageService;

    public void onMessage(final Message message) {
        Long delay = 0L;
        String messageId = null;
        int retryCount = 0;
        String domainCode = null;
        try {
            messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            if (message.propertyExists(MessageConstants.RETRY_COUNT)) {
                retryCount = message.getIntProperty(MessageConstants.RETRY_COUNT);
            }
            domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            delay = message.getLongProperty(MessageConstants.DELAY);
        } catch (final NumberFormatException nfe) {
            //This is ok, no delay has been set
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
        if(StringUtils.isBlank(messageId)) {
            LOG.error("Message ID is empty: could not send message");
            return;
        }
        if(StringUtils.isBlank(domainCode)) {
            LOG.error("Domain is empty: could not send message");
            return;
        }

        LOG.debug("Sending message ID [{}] for domain [{}]", messageId, domainCode);
        domainContextProvider.setCurrentDomain(domainCode);
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);

        if (delay > 0) {
            userMessageService.scheduleSending(messageId, delay);
            return;
        }

        messageSenderService.sendUserMessage(messageId, retryCount);
    }
}
