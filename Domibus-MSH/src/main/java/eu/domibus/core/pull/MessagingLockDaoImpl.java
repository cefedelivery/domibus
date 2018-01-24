package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
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
public class MessagingLockDaoImpl implements MessagingLockDao{

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    @Override
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcess", MessagingLock.class);
        namedQuery.setParameter("MESSAGE_TYPE", messageType);
        namedQuery.setParameter("INITIATOR", initiator);
        namedQuery.setParameter("MPC", mpc);
        namedQuery.setParameter("MESSAGE_STATE", READY);
        return getMessageId(namedQuery);
    }

    private String getMessageId(TypedQuery<MessagingLock> namedQuery) {
        try {
            MessagingLock messagingLock = namedQuery.getSingleResult();
            lockMessage(messagingLock);
            return messagingLock.getMessageId();
        }catch (NoResultException e){
            return  null;
        }
    }

    @Override
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc, final List<Integer> lockedIds) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcess", MessagingLock.class);
        namedQuery.setParameter("MESSAGE_TYPE", messageType);
        namedQuery.setParameter("INITIATOR", initiator);
        namedQuery.setParameter("MPC", mpc);
        namedQuery.setParameter("MESSAGE_STATE", READY);
        namedQuery.setParameter("LOCKED_IDS", lockedIds);
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
    public void save(MessagingLock messagingLock){
        entityManager.persist(messagingLock);
    }
}
