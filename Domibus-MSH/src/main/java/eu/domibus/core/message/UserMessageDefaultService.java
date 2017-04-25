package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.ebms3.common.UserMessageServiceHelper;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    UserMessageLogDao userMessageLogDao;

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    UserMessageServiceHelper userMessageServiceHelper;

    @Override
    public String getFinalRecipient(String messageId) {
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if(userMessage == null) {
            LOG.debug("Message [{}] does not exist", messageId);
            return null;
        }
        return userMessageServiceHelper.getFinalRecipient(userMessage);
    }

    @Override
    public List<String> getFailedMessages() {
        return userMessageLogDao.findFailedMessages(null);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient) {
        LOG.debug("Provided finalRecipient is [{}]", finalRecipient);
        return userMessageLogDao.findFailedMessages(finalRecipient);
    }

    @Override
    public Long getFailedMessageElapsedTime(String messageId) {
        return null;//messagingDao.getFailedMessageElapsedTime(messageId);
    }

    @Override
    public void restoreFailedMessage(String messageId) {

    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end) {
        return null;
    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end, String finalRecipient) {
        return null;
    }

    @Override
    public void deleteFailedMessage(String messageId) {

    }
}
