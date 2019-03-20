package eu.domibus.core.pull;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLogEntity;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * @author Thomas Dussart
 * @since 3.3.3
 * <p>
 * {@inheritDoc}
 */
@Service
public class PullMessageStateServiceImpl implements PullMessageStateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullMessageStateServiceImpl.class);

    @Autowired
    protected RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected MessagingDao messagingDao;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expirePullMessage(final String messageId) {
        LOG.debug("Message:[{}] expired.", messageId);
        final UserMessageLogEntity userMessageLog = userMessageLogDao.findByMessageId(messageId);
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageId);
        sendFailed(userMessageLog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void sendFailed(final UserMessageLogEntity userMessageLog) {
        LOG.debug("Message:[{}] failed to be pull.", userMessageLog.getMessageId());
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(userMessageLog.getMessageId());
        updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final UserMessageLogEntity userMessageLog) {
        final MessageStatus readyToPull = MessageStatus.READY_TO_PULL;
        LOG.debug("Change message:[{}] with state:[{}] to state:[{}].", userMessageLog.getMessageId(), userMessageLog.getMessageStatus(), readyToPull);
        userMessageLog.setMessageStatus(readyToPull);
        userMessageLogDao.update(userMessageLog);
        uiReplicationSignalService.messageStatusChange(userMessageLog.getMessageId(), readyToPull);
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.READY_TO_PULL, new Timestamp(System.currentTimeMillis()));
    }


}
