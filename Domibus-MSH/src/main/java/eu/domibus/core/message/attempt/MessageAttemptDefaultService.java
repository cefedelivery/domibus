package eu.domibus.core.message.attempt;

import com.codahale.metrics.Timer;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.api.metrics.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.codahale.metrics.MetricRegistry.name;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public MessageAttempt create(MessageAttempt attempt) {
        final MessageAttemptEntity entity = domainCoreConverter.convert(attempt, MessageAttemptEntity.class);

        messageAttemptDao.create(entity);

        try {
            return domainCoreConverter.convert(entity, MessageAttempt.class);
        } finally {
        }
    }
}
