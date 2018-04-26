
package eu.domibus.plugin.handler;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageStatus;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;

import java.util.List;

/**
 * Implementations of this interface handle the retrieval of messages from
 * Domibus to the backend.
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public interface MessageRetriever {

    /**
     * provides the message with the corresponding messageId
     *
     * @param messageId the messageId of the message to retrieve
     * @return the message object with the given messageId
     */
    Submission downloadMessage(String messageId) throws MessageNotFoundException;

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageId id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     */
    MessageStatus getStatus(String messageId);

    /**
     * Returns List {@link java.util.List} of error logs {@link ErrorResult} for message with messageid
     *
     * @param messageId id of the message the errors are requested for
     * @return the list of error log entries {@link java.util.List< ErrorResult >}
     */
    List<? extends ErrorResult> getErrorsForMessage(String messageId);
}
