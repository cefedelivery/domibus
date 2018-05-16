package eu.domibus.core.pull;

import eu.domibus.common.model.logging.UserMessageLog;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * In the case of a pull mechanism when a message is retrieved, there is a need for a lock mechanism to occur in order
 * to avoid that a message is pulled twice. This service is in charge of this behavior.
 */
public interface PullMessageStateService {


    /**
     * Handle the state management of a staled pull message.²
     * @param messageId
     */
    void messageStaled(String messageId);

    /**
     * Reset the next attempt date, put the message in send_failure and notify if configure.
     *
     * @param userMessageLog
     */
    void sendFailed(UserMessageLog userMessageLog);
}
