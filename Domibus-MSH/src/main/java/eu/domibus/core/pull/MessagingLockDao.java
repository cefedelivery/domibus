package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;
import org.springframework.stereotype.Repository;

import javax.persistence.*;

import static javax.persistence.LockModeType.PESSIMISTIC_FORCE_INCREMENT;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class MessagingLockDao {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    public Integer getNexMessageToProcess(final String messageType) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcess", MessagingLock.class);
        namedQuery.setParameter("MESSAGE_TYPE", messageType);
        MessagingLock messageToProcess = namedQuery.getSingleResult();
        try {
            entityManager.lock(messageToProcess, PESSIMISTIC_FORCE_INCREMENT);
            return null;
        } catch (LockTimeoutException | PessimisticLockException e) {
            return messageToProcess.getEntityId();
        }
    }
}
