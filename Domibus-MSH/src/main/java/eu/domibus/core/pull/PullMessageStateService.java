package eu.domibus.core.pull;

import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.UserMessage;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * In the case of a pull mechanism when a message is retrieved, there is a need for a lock mechanism to occur in order
 * to avoid that a message is pulled twice. This service is in charge of this behavior.
 */
public interface PullMessageStateService {


    /**
     * Handle the state management of a staled pull message.
     * @param messageId
     */
    void messageStaled(String messageId);

    @Transactional
    void sendFailed(UserMessageLog userMessageLog);
}
