package eu.domibus.core.pull;

import com.codahale.metrics.Timer;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.List;

import static com.codahale.metrics.MetricRegistry.name;
import static eu.domibus.api.metrics.Metrics.METRIC_REGISTRY;
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
        Timer.Context getFirstNextPullMessageToProcess = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.getFirstNextPullMessageToProcess")).time();
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcess", MessagingLock.class);
        namedQuery.setParameter(MESSAGE_TYPE, messageType);
        namedQuery.setParameter(INITIATOR, initiator);
        namedQuery.setParameter(MPC, mpc);
        namedQuery.setParameter(MESSAGE_STATE, READY);
        namedQuery.setFirstResult(0);
        namedQuery.setMaxResults(1);
        LOG.debug("Retrieving message of type:[{}] and state:[{}] for initiator:[{}] and mpc:[{}]", messageType, READY, initiator, mpc);
        String messageId = getMessageId(namedQuery);
        getFirstNextPullMessageToProcess.stop();
        return messageId;
    }

    private String getMessageId(TypedQuery<MessagingLock> namedQuery) {
        Timer.Context getMessageId = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.getMessageId")).time();
        try {
            MessagingLock messagingLock = namedQuery.getSingleResult();
            LOG.debug("Message retrieved:   \n[{}]", messagingLock);
            Timer.Context lockMessage = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "lockMessage")).time();
            lockMessage(messagingLock);
            lockMessage.close();
            LOG.debug("Message wit id:[{}] locked", messagingLock.getMessageId());
            String messageId = messagingLock.getMessageId();
            return messageId;
        } catch (NoResultException e) {
            LOG.debug("No message found");
            return null;
        }
        finally {
            getMessageId.close();
        }
    }

    @Override
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc, final List<Integer> lockedIds) {
        Timer.Context getAfterLockNextPullMessageToProcess = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.getAfterLockNextPullMessageToProcess")).time();
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

        String messageId = getMessageId(namedQuery);
        getAfterLockNextPullMessageToProcess.stop();
        return messageId;
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
        Timer.Context deleteMessageLock = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.deleteMessageLock")).time();
        Query query = entityManager.createNamedQuery("MessagingLock.delete");
        query.setParameter(MESSAGE_ID,messageId);
        query.executeUpdate();
        deleteMessageLock.close();
    }

    @Override
    public void updateStatus(final String messageId,final MessageState messageState) {
        Timer.Context updateStatus = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.updateStatus")).time();
        Query query = entityManager.createNamedQuery("MessagingLock.updateStatus");
        query.setParameter(MESSAGE_ID,messageId);
        query.setParameter(MESSAGE_STATE,messageState);
        query.executeUpdate();
        updateStatus.stop();
    }
}
