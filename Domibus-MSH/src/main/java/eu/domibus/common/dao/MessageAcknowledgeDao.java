package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.MessageAcknowledge;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

/**
 * Created by migueti on 15/03/2017.
 */
@Repository
@Transactional
public class MessageAcknowledgeDao extends BasicDao<MessageAcknowledge> implements IMessageAcknowledgeDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgeDao.class);

    public MessageAcknowledgeDao() {
        super(MessageAcknowledge.class);
    }

    public MessageAcknowledge findByMessageId(String messageId) {
        try {
            final TypedQuery<MessageAcknowledge> query = em.createNamedQuery("MessageAcknowledge.findMessageAcknowledgeByMessageId",
                    MessageAcknowledge.class);
            query.setParameter("FK_MESSAGE_ID", messageId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for message id[" + messageId + "]", e);
            return null;
        }
    }

    public MessageAcknowledge findByFrom(String from) {
        try {
            final TypedQuery<MessageAcknowledge> query = em.createNamedQuery("MessageAcknowledge.findMessageAcknowledgeByFrom",
                    MessageAcknowledge.class);
            query.setParameter("FROM", from);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for from[" + from + "]", e);
            return null;
        }
    }

    public MessageAcknowledge findByTo(String to) {
        try {
            final TypedQuery<MessageAcknowledge> query = em.createNamedQuery("MessageAcknowledge.findMessageAcknowledgeByTo",
                    MessageAcknowledge.class);
            query.setParameter("TO", to);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for to[" + to + "]", e);
            return null;
        }
    }
}
