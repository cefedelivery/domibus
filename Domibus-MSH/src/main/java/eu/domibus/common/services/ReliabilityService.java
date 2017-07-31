package eu.domibus.common.services;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ReliabilityService {
    /**
     * Method supposed to be called in a finally block after pushing or being pulled.
     * It will handle the notifications and increase of messages attempts.
     *
     * @param messageId                  the processed message id.
     * @param reliabilityCheckSuccessful the state of the reliability check.
     * @param isOk                       sub status when reliability is ok.
     * @param legConfiguration           the legconfiguration of this message exchange.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void handleReliability(String messageId, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, ResponseHandler.CheckResult isOk, LegConfiguration legConfiguration);
}
