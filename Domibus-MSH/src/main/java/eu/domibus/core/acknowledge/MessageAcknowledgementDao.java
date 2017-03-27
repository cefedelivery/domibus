package eu.domibus.core.acknowledge;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Repository
public class MessageAcknowledgementDao extends BasicDao<MessageAcknowledgementEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    public MessageAcknowledgementDao() {
        super(MessageAcknowledgementEntity.class);
    }

    public List<MessageAcknowledgementEntity> findByMessageId(String messageId) {
        try {
            final TypedQuery<MessageAcknowledgementEntity> query = em.createNamedQuery("MessageAcknowledgement.findMessageAcknowledgementByMessageId",
                    MessageAcknowledgementEntity.class);
            query.setParameter("MESSAGE_ID", messageId);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for message id[" + messageId + "]");
            return null;
        }
    }
}
