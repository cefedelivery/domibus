package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Repository
public class MessagingLockDaoImpl implements MessagingLockDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockDaoImpl.class);

    public static final String MPC = "MPC";

    public static final String MESSAGE_ID = "MESSAGE_ID";

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    @Autowired
    private GetNextMessageProcedure getNextMessageProcedure;

    @Override
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc) {
        String messageId = getNextMessageProcedure.callProcedure(messageType, initiator, mpc);
        if (messageId != null) {
            LOG.debug("Retrieving messages with id[{}], type:[{}], inititator [{}], mpc:[{}]", messageId, messageType, initiator, mpc);
        }
        return messageId;
    }

    @Override
    public void save(MessagingLock messagingLock) {
        entityManager.persist(messagingLock);
    }

    @Override
    public void delete(final String messageId) {
        Query query = entityManager.createNamedQuery("MessagingLock.delete");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }

}
