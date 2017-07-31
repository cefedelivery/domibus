package eu.domibus.common.services.impl;

import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
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

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private MessageExchangeService messageExchangeService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePushReliability(String messageId, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, ResponseHandler.CheckResult isOk, LegConfiguration legConfiguration) {
        reliabilityChecker.handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = ReliabilityException.class)
    public void handlePullReliability(String messageId, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, ResponseHandler.CheckResult isOk, LegConfiguration legConfiguration) {
        messageExchangeService.removeRawMessageIssuedByPullRequest(messageId);
        reliabilityChecker.handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
    }


}
