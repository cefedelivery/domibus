package eu.domibus.core.pull;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class MessagingLockServiceImpl implements MessagingLockService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockServiceImpl.class);

    @Autowired
    private MessagingLockDao messagingLockDao;

    private final static int maxTentative = 3;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getPullMessageToProcess(final String initiator, final String mpc) {

        return messagingLockDao.getNextPullMessageToProcess(MessagingLock.PULL, initiator, mpc, 100);

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
    public void addLockingInformation(final Party initiator, final String messageId, final String mpc) {
        Set<Identifier> identifiers = initiator.getIdentifiers();
        //TODO throw an exception here.
        if (identifiers.size() == 0) {
            LOG.warn("No identifier defined for party:[{}], the message will not be available for pulling", initiator.getName());
            return;
        }
        Identifier identifier = identifiers.iterator().next();
        LOG.debug("Saving messagelock with id:[{}],partyID:[{}], mpc:[{}]",messageId,identifier.getPartyId(),mpc);
        MessagingLock messagingLock = new MessagingLock(messageId, identifier.getPartyId(), mpc);
        messagingLockDao.save(messagingLock);
    }

    @Override
    @Transactional
    public void delete(final String messageId) {
        messagingLockDao.delete(messageId);
    }



}