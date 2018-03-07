package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class MessagingLockServiceImpl implements MessagingLockService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockServiceImpl.class);

    @Autowired
    @Qualifier("dMessagingLock")
    private MessagingLockDao messagingLockDao;


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getPullMessageToProcess(final String initiator, final String mpc) {

        return messagingLockDao.getNextPullMessageToProcess(MessagingLock.PULL, initiator, mpc);

    }

    /*private String tryAnotherMessage(final String initiator, final String mpc, final List<Integer> lockedIds) {
        if (lockedIds.size() >= maxTentative) {
            LOG.error("Max tentative:[{}] to lock a message has been reached for initiator:[{}] and mpc:[{}]"+ maxTentative,initiator,mpc);
            throw new MessagingLockException("Max tentative to lock a message has been reached:" + maxTentative);
        }
        try {
            return messagingLockDao.getNextPullMessageToProcess(MessagingLock.PULL, initiator, mpc, lockedIds);
        } catch (MessagingLockException e) {
            assert e.getMessageAlreadyLockedId() != null;
            lockedIds.add(e.getMessageAlreadyLockedId());
            LOG.error("Messaging locking mechanism for initiator:[{}] and mpc:[{}], retrying without ids[{}]", initiator,mpc,StringUtils.join(lockedIds, ","));
            return tryAnotherMessage(initiator, mpc, lockedIds);
        }
    }*/

    @Override
    @Transactional
    public void addLockingInformation(final PartyIdExtractor partyIdExtractor, final String messageId, final String mpc) {

        String partyId = partyIdExtractor.getPartyId();
        LOG.debug("Saving messagelock with id:[{}],partyID:[{}], mpc:[{}]",messageId, partyId,mpc);
        MessagingLock messagingLock = new MessagingLock(messageId, partyId, mpc);
        messagingLockDao.save(messagingLock);
    }

    @Override
    @Transactional
    public void delete(final String messageId) {
        messagingLockDao.delete(messageId);
    }



}
