package eu.domibus.common.services;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;

/**
 * Service in charge or handling the states of messages exchanges being pull or push.
 * Those methods are supposed to be executed what ever the result of the exchange as they are in charge
 * of message's state.
 *
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ReliabilityService {
    /**
     * Method supposed to be called after pushing or pulling.
     * It will handle the notifications and increase of messages attempts.
     *
     * @param messageId                  the processed message id.
     * @param reliabilityCheckSuccessful the state of the reliability check.
     * @param isOk                       sub status when reliability is ok.
     * @param legConfiguration           the legconfiguration of this message exchange.
     */
    void handleReliability(String messageId, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, ResponseHandler.CheckResult isOk, LegConfiguration legConfiguration);


    /**
     * Method supposed to be called after the receive of the pull request receipt.
     * It will handle the notifications and increase of messages attempts.
     * In the case of a pull request, the transaction that handle the reliability should also remove the raw message saved in order to validate
     * the non repudiation message.
     *
     * @param messageId
     * @param reliabilityCheckSuccessful
     * @param isOk
     * @param legConfiguration
     */
    void handlePullReceiptReliability(String messageId, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, ResponseHandler.CheckResult isOk, LegConfiguration legConfiguration);
}
