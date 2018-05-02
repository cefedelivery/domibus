package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public List<MessageAttempt> getAttemptsHistory(String messageId) {
        final List<MessageAttemptEntity> entities = messageAttemptDao.findByMessageId(messageId);
        return domainCoreConverter.convert(entities, MessageAttempt.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void create(MessageAttempt attempt) {
        if (isMessageAttemptAuditDisabled()) {
            return;
        }

        final MessageAttemptEntity entity = domainCoreConverter.convert(attempt, MessageAttemptEntity.class);
        messageAttemptDao.create(entity);
    }

    protected boolean isMessageAttemptAuditDisabled() {
        String messageAttemptAuditEnabled = domibusPropertyProvider.getProperty("domibus.sendMessage.attempt.audit.active", "true");
        return !BooleanUtils.toBoolean(messageAttemptAuditEnabled);
    }
}
