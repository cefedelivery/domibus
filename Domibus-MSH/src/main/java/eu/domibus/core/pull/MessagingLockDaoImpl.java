package eu.domibus.core.pull;

import com.codahale.metrics.Timer;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private GetNextMessageProcedure getNextMessageProcedure;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc, int maxResult) {
        String messageId = getNextMessageProcedure.callProcedure(messageType, initiator, mpc);
        if(messageId!=null) {
            LOG.debug("Retrieving messages with id[{}], type:[{}], inititator [{}], mpc:[{}]",messageId,messageType, initiator, mpc);
        }
        return messageId;
        /*String lockedMessageID=null;
        Query nativeQuery = this.entityManager.createNativeQuery("{call get_next(?,?,?,?)}");
        nativeQuery.setParameter(1, messageType);
        nativeQuery.setParameter(2, initiator);
        nativeQuery.setParameter(3, mpc);
        nativeQuery.setParameter(4, lockedMessageID);
        nativeQuery.executeUpdate();
        if(lockedMessageID!=null) {
            LOG.debug("Database locked message with id [{}]",lockedMessageID);
        }
        else {
            LOG.debug("No message found");
        }
        return lockedMessageID;
*/

        /*Double sum = (Double) query.getOutputParameterValue("sum");
        Timer.Context getFirstNextPullMessageToProcess = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.getFirstNextPullMessageToProcess")).time();
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcess", MessagingLock.class);
        namedQuery.setParameter(MESSAGE_TYPE, messageType);
        namedQuery.setParameter(INITIATOR, initiator);
        namedQuery.setParameter(MPC, mpc);
        namedQuery.setParameter(MESSAGE_STATE, READY);
        namedQuery.setFirstResult(0);
        namedQuery.setMaxResults(maxResult);
        //LOG.debug("Retrieving message of type:[{}] and state:[{}] for initiator:[{}] and mpc:[{}]", messageType, READY, initiator, mpc);
        String messageId = getMessageId(namedQuery);
        getFirstNextPullMessageToProcess.stop();
        return messageId;*/
    }

    private String getMessageId(TypedQuery<MessagingLock> namedQuery) {
        Timer.Context getMessageId = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.getMessageId")).time();
        try {
            List<MessagingLock> messagingLockList = namedQuery.getResultList();
            if(messagingLockList.size()==0){
                return null;
            }
            int maxIndex=messagingLockList.size()-1;
            int min=0;
            int randomIndex= (int) ((Math.random()*(maxIndex-min))+min);
            LOG.debug("messagingLockList sise:[{}], peaking:[{}]",messagingLockList.size(),randomIndex);
            MessagingLock messagingLock=messagingLockList.get(randomIndex);
          //  LOG.debug("Message retrieved:   \n[{}]", messagingLock);
            Timer.Context lockMessage = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "lockMessage")).time();
            if(!lockMessage(messagingLock)){
                return null;
            }
            lockMessage.close();
            //LOG.debug("Message wit id:[{}] locked", messagingLock.getMessageId());
            String messageId = messagingLock.getMessageId();
            return messageId;
        } catch (NoResultException e) {
            //LOG.debug("No message found");
            return null;
        }
        finally {
            getMessageId.close();
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,noRollbackFor = MessagingLockException.class)
    public String getNextPullMessageToProcess(final String messageType, final String initiator, final String mpc, final List<Integer> lockedIds) {
        Timer.Context getAfterLockNextPullMessageToProcess = METRIC_REGISTRY.timer(name(MessagingLockDaoImpl.class, "pull.getAfterLockNextPullMessageToProcess")).time();
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findNexMessageToProcessExcludingLocked", MessagingLock.class);
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

        String messageId=null;
        try {
            messageId = getMessageId(namedQuery);
        }catch (MessagingLockException e){
            return messageId;
        }
        getAfterLockNextPullMessageToProcess.stop();
        return messageId;
    }

    private boolean lockMessage(MessagingLock messagingLock) {
        try {
            /**LOG.warn("Locking message for:[{}]",messagingLock.getMessageId());
            try {
                LOG.warn("GO to sleep for 10 seconds");
                Thread.sleep(10000);
                LOG.warn("Done!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            LOG.debug("Locking message for:[{}] with state[{}]",messagingLock.getMessageId(),messagingLock.getMessageState());
            entityManager.lock(messagingLock, PESSIMISTIC_WRITE);
            return true;

          /*  if(count==0){
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
            }else{
                count=0;
            }*/
            //messagingLock.setMessageState(MessageState.PROCESSING);
            //entityManager.merge(messagingLock);
        //} catch (LockTimeoutException | PessimisticLockException |OptimisticLockException e) {
        } catch (Exception e) {
            return false;
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



}