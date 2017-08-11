package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.core.converter.DomainCoreConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Properties;

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

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Override
    public List<MessageAttempt> getAttemptsHistory(String messageId) {
        final List<MessageAttemptEntity> entities = messageAttemptDao.findByMessageId(messageId);
        return domainCoreConverter.convert(entities, MessageAttempt.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public MessageAttempt create(MessageAttempt attempt) {
        if(isMessageAttemptAuditDisabled()) {
            return null;
        }

        final MessageAttemptEntity entity = domainCoreConverter.convert(attempt, MessageAttemptEntity.class);
        messageAttemptDao.create(entity);
        return domainCoreConverter.convert(entity, MessageAttempt.class);
    }

    protected boolean isMessageAttemptAuditDisabled() {
        String messageAttemptAuditEnabled = domibusProperties.getProperty("domibus.sendMessage.attempt.audit.active", "true");
        return !Boolean.valueOf(messageAttemptAuditEnabled);
    }
}
