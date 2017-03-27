package eu.domibus.core.acknowledge;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Repository
public class MessageAcknowledgementDefaultDao extends BasicDao<MessageAcknowledgementEntity> implements MessageAcknowledgementDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    public MessageAcknowledgementDefaultDao() {
        super(MessageAcknowledgementEntity.class);
    }

    public MessageAcknowledgementEntity findByMessageId(String messageId) {
        try {
            final TypedQuery<MessageAcknowledgementEntity> query = em.createNamedQuery("MessageAcknowledgement.findMessageAcknowledgementByMessageId",
                    MessageAcknowledgementEntity.class);
            query.setParameter("MESSAGE_ID", messageId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for message id[" + messageId + "]");
            return null;
        }
    }

    public MessageAcknowledgementEntity findByFrom(String from) {
        try {
            final TypedQuery<MessageAcknowledgementEntity> query = em.createNamedQuery("MessageAcknowledgement.findMessageAcknowledgementByFrom",
                    MessageAcknowledgementEntity.class);
            query.setParameter("FROM", from);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for from[" + from + "]");
            return null;
        }
    }

    public MessageAcknowledgementEntity findByTo(String to) {
        try {
            final TypedQuery<MessageAcknowledgementEntity> query = em.createNamedQuery("MessageAcknowledgement.findMessageAcknowledgementByTo",
                    MessageAcknowledgementEntity.class);
            query.setParameter("TO", to);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for to[" + to + "]");
            return null;
        }
    }
}
