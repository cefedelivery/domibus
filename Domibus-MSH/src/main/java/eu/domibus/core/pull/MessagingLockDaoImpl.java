package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.List;

import static eu.domibus.ebms3.common.model.MessageState.READY;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class MessagingLockDaoImpl implements MessagingLockDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockDaoImpl.class);

    private static final String MESSAGE_TYPE = "MESSAGE_TYPE";

    private static final String INITIATOR = "INITIATOR";

    public static final String MPC = "MPC";

    private static final String MESSAGE_STATE = "MESSAGE_STATE";

    private static final String LOCKED_IDS = "LOCKED_IDS";

    public static final String MESSAGE_ID = "MESSAGE_ID";

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    @Override
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcess", MessagingLock.class);
        namedQuery.setParameter(MESSAGE_TYPE, messageType);
        namedQuery.setParameter(INITIATOR, initiator);
        namedQuery.setParameter(MPC, mpc);
        namedQuery.setParameter(MESSAGE_STATE, READY);
        namedQuery.setFirstResult(0);
        namedQuery.setMaxResults(1);
        LOG.debug("Retrieving message of type:[{}] and state:[{}] for initiator:[{}] and mpc:[{}]", messageType, READY, initiator, mpc);
        return getMessageId(namedQuery);
    }

    private String getMessageId(TypedQuery<MessagingLock> namedQuery) {
        try {
            MessagingLock messagingLock = namedQuery.getSingleResult();
            LOG.debug("Message retrieved:   \n[{}]", messagingLock);
            lockMessage(messagingLock);
            LOG.debug("Message wit id:[{}] locked", messagingLock.getMessageId());
            return messagingLock.getMessageId();
        } catch (NoResultException e) {
            LOG.debug("No message found");
            return null;
        }
    }

    @Override
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc, final List<Integer> lockedIds) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcess", MessagingLock.class);
        namedQuery.setParameter(MESSAGE_TYPE, messageType);
        namedQuery.setParameter(INITIATOR, initiator);
        namedQuery.setParameter(MPC, mpc);
        namedQuery.setParameter(MESSAGE_STATE, READY);
        namedQuery.setParameter(LOCKED_IDS, lockedIds);
        namedQuery.setFirstResult(0);
        namedQuery.setMaxResults(1);
        LOG.debug("Retrieving message of type:[{}] and state:[{}] for initiator:[{}] and mpc:[{}] and not with ids:", messageType, READY, initiator, mpc);
        if (LOG.isDebugEnabled()) {
            for (Integer lockedId : lockedIds) {
                LOG.debug("id[{}]", lockedId);
            }
        }

        return getMessageId(namedQuery);
    }

    private void lockMessage(MessagingLock messagingLock) {
        try {
            entityManager.lock(messagingLock, PESSIMISTIC_WRITE);
            messagingLock.setMessageState(MessageState.PROCESSING);
            entityManager.merge(messagingLock);
        } catch (LockTimeoutException | PessimisticLockException e) {
            throw new MessagingLockException(messagingLock.getEntityId());
        }
    }

    @Override
    public void save(MessagingLock messagingLock) {
        entityManager.persist(messagingLock);
    }

    @Override
    public void delete(final String messageId) {
        Query query = entityManager.createNamedQuery("MessagingLock.delete");
        query.setParameter(MESSAGE_ID,messageId);
        query.executeUpdate();
    }

    @Override
    public void updateStatus(final String messageId,final MessageState messageState) {
        Query query = entityManager.createNamedQuery("MessagingLock.updateStatus");
        query.setParameter(MESSAGE_ID,messageId);
        query.setParameter(MESSAGE_STATE,messageState);
        query.executeUpdate();
    }
}
