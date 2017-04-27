package eu.domibus.core.message.attempt;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class MessageAttemptDao extends BasicDao<MessageAttemptEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAttemptDao.class);

    public MessageAttemptDao() {
        super(MessageAttemptEntity.class);
    }

    public List<MessageAttemptEntity> findByMessageId(String messageId) {
        try {
            final TypedQuery<MessageAttemptEntity> query = em.createNamedQuery("MessageAttemptEntity.findAttemptsByMessageId", MessageAttemptEntity.class);
            query.setParameter("MESSAGE_ID", messageId);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message attempts for message id[" + messageId + "]");
            return null;
        }
    }
}
