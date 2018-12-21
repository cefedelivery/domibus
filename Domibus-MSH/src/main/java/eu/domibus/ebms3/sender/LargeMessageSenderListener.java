package eu.domibus.ebms3.sender;

import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service(value = "largeMessageSenderListener")
public class LargeMessageSenderListener extends AbstractMessageSenderListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LargeMessageSenderListener.class);

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        LOG.debug("Processing large message [{}]", message);

        try {
            String messageType = message.getStringProperty(UserMessageService.MSG_TYPE);
            if (StringUtils.equals(messageType, UserMessageService.MSG_SOURCE_USER_MESSAGE_REJOIN)) {
                String domainCode = null;
                try {
                    domainCode = message.getStringProperty(MessageConstants.DOMAIN);
                } catch (final JMSException e) {
                    LOG.error("Error processing JMS message", e);
                }
                if(StringUtils.isBlank(domainCode)) {
                    LOG.error("Domain is empty: could not send message");
                    return;
                }

                domainContextProvider.setCurrentDomain(domainCode);
                final String groupId = message.getStringProperty(UserMessageService.MSG_GROUP_ID);
                splitAndJoinService.rejoinSourceMessage(groupId);
            } else {
                super.onMessage(message);
            }
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }

    }
}
