package eu.domibus.core.pull;

import com.google.common.collect.Lists;
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

import java.util.List;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getPullMessageToProcess(final String initiator, final String mpc) {
        try {
            return messagingLockDao.getNextPullMessageToProcess(MessagingLock.PULL, initiator, mpc);
        } catch (MessagingLockException e) {
            List<Integer> lockedIds = Lists.newArrayList(e.getMessageAlreadyLockedId());
            return tryAnotherMessage(initiator, mpc, lockedIds);
        }
    }

    private String tryAnotherMessage(final String initiator, final String mpc, final List<Integer> lockedIds) {
        if (lockedIds.size() >= maxTentative) {
            throw new MessagingLockException("Max tentative to lock a message has been reached:" + maxTentative);
        }
        try {
            return messagingLockDao.getNextPullMessageToProcess(MessagingLock.PULL, initiator, mpc, lockedIds);
        } catch (MessagingLockException e) {
            assert e.getMessageAlreadyLockedId() != null;
            lockedIds.add(e.getMessageAlreadyLockedId());
            return tryAnotherMessage(initiator, mpc, lockedIds);
        }
    }

    @Override
    @Transactional
    public void addLockingInformation(final Party initiator, final String messageId, final String mpc) {
        Set<Identifier> identifiers = initiator.getIdentifiers();
        if (identifiers.size() == 0) {
            LOG.warn("No identifier defined for party:[{}], the message will not be available for pulling", initiator.getName());
            return;
        }
        Identifier identifier = identifiers.iterator().next();
        MessagingLock messagingLock = new MessagingLock(messageId, identifier.getPartyId(), mpc);
        messagingLockDao.save(messagingLock);
    }

    @Override
    @Transactional
    public void delete(final String messageId) {
        messagingLockDao.delete(messageId);
    }

    @Override
    @Transactional
    public void rollback(final String messageId) {
        messagingLockDao.updateStatus(messageId, MessageState.READY);
    }

}
