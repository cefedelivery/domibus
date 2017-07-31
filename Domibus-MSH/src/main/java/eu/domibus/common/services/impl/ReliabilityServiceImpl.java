package eu.domibus.common.services.impl;

import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Service
public class ReliabilityServiceImpl implements ReliabilityService {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ReliabilityServiceImpl.class);

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBusinessMessageReliability(final String messageId, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, final ResponseHandler.CheckResult isOk, final LegConfiguration legConfiguration) {
        handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = ReliabilityException.class)
    public void handlePullReceiptReliability(final String messageId, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, final ResponseHandler.CheckResult isOk, final LegConfiguration legConfiguration) {
        try {
            messageExchangeService.removeRawMessageIssuedByPullRequest(messageId);
        } catch (ReliabilityException e) {
            LOG.warn("There should be a raw xml entry for this message.");
        }
        handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
    }

    private void handleReliability(String messageId, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, ResponseHandler.CheckResult isOk, LegConfiguration legConfiguration) {
        switch (reliabilityCheckSuccessful) {
            case OK:
                switch (isOk) {
                    case OK:
                        userMessageLogDao.setMessageAsAcknowledged(messageId);
                        break;
                    case WARNING:
                        userMessageLogDao.setMessageAsAckWithWarnings(messageId);
                        break;
                    default:
                        assert false;
                }
                backendNotificationService.notifyOfSendSuccess(messageId);
                userMessageLogDao.setAsNotified(messageId);
                messagingDao.clearPayloadData(messageId);
                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS);
                break;
            case WAITING_FOR_CALLBACK:
                userMessageLogDao.setMessageAsWaitingForReceipt(messageId);
                break;
            case SEND_FAIL:
                updateRetryLoggingService.updatePushedMessageRetryLogging(messageId, legConfiguration);
                break;
            case PULL_FAILED:
                updateRetryLoggingService.updatePulledMessageRetryLogging(messageId, legConfiguration);
                break;
        }
    }


}
