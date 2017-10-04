package eu.domibus.common.dao;

import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@Repository
public class SignalMessageDao extends BasicDao<SignalMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageDao.class);

    public SignalMessageDao() {
        super(SignalMessage.class);
    }

    public List<SignalMessage> findSignalMessagesByRefMessageId(final String originalMessageId) {
        try {
            final TypedQuery<SignalMessage> query = em.createNamedQuery("SignalMessage.findSignalMessageByRefMessageId", SignalMessage.class);
            query.setParameter("ORI_MESSAGE_ID", originalMessageId);
            return query.getResultList();
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any signal message for original message id[" + originalMessageId + "]");
            return null;
        }
    }

    public List<String> findSignalMessageIdsByRefMessageId(final String originalMessageId) {
        try {
            final TypedQuery<String> query = em.createNamedQuery("SignalMessage.findSignalMessageIdByRefMessageId", String.class);
            query.setParameter("ORI_MESSAGE_ID", originalMessageId);
            return query.getResultList();
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any signal message id for original message id[" + originalMessageId + "]");
            return null;
        }
    }

    /**
     * Clear receipts of the Signal Message.
     *
     * @param signalMessage
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void clear(final SignalMessage signalMessage) {
        if (signalMessage.getReceipt() != null) {
            signalMessage.getReceipt().getAny().clear();
        }
        signalMessage.setReceipt(null);
        update(signalMessage);
        LOG.debug("Xml data for signal message [" + signalMessage.getMessageInfo().getMessageId() + "] have been cleared");
    }


}
