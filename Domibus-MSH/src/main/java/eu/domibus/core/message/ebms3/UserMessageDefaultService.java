package eu.domibus.core.message.ebms3;

import eu.domibus.api.message.ebms3.UserMessageService;
import eu.domibus.api.message.ebms3.model.UserMessage;
import eu.domibus.common.dao.MessagingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageDefaultService implements UserMessageService {


    @Autowired
    private MessagingDao messagingDao;

    @Override
    public UserMessage getMessage(final String messageId) {
        return messagingDao.findUserMessageByMessageId(messageId);
    }
}
