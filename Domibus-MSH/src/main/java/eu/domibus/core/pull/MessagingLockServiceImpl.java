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
 * @since 3.3.3
 *
 *{@inheritDoc}
 *
 */
@Service
public class MessagingLockServiceImpl implements MessagingLockService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockServiceImpl.class);

    @Autowired
    @Qualifier("dMessagingLock")
    private MessagingLockDao messagingLockDao;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getPullMessageId(final String initiator, final String mpc) {

        return messagingLockDao.getNextPullMessageToProcess(MessagingLock.PULL, initiator, mpc);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addSearchInFormation(final PartyIdExtractor partyIdExtractor, final String messageId, final String mpc) {
        String partyId = partyIdExtractor.getPartyId();
        LOG.debug("Saving messagelock with id:[{}],partyID:[{}], mpc:[{}]",messageId, partyId,mpc);
        MessagingLock messagingLock = new MessagingLock(messageId, partyId, mpc);
        messagingLockDao.save(messagingLock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(final String messageId) {
        messagingLockDao.delete(messageId);
    }

}
