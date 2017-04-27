package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.core.converter.DomainCoreConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAttemptDefaultService implements MessageAttemptService {

    @Autowired
    MessageAttemptDao messageAttemptDao;

    @Autowired
    DomainCoreConverter domainCoreConverter;

    @Override
    public List<MessageAttempt> getAttemptsHistory(String messageId) {
        final List<MessageAttemptEntity> entities = messageAttemptDao.findByMessageId(messageId);
        return domainCoreConverter.convert(entities, MessageAttempt.class);
    }
}
