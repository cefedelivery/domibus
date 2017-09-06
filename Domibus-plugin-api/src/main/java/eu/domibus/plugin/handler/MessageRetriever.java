
package eu.domibus.plugin.handler;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageStatus;
import eu.domibus.messaging.MessageNotFoundException;

import java.util.List;

/**
 * Implementations of this interface handle the retrieval of messages from
 * Domibus to the backend.
 *
 * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 4.0
 *
 * @param <T> Data transfer object
 *            (http://en.wikipedia.org/wiki/Data_transfer_object) transported between the
 *            backend and Domibus
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Deprecated
public interface MessageRetriever<T> {

    /**
     * provides the message with the corresponding messageId
     *
     * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 4.0
     *
     * @param messageId the messageId of the message to retrieve
     * @return the message object with the given messageId
     */
    @Deprecated
    T downloadMessage(String messageId) throws MessageNotFoundException;

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageId id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     * @deprecated since 3.3-rc1; this method converts DOWNLOADED status to RECEIVED to maintain
     * the backwards compatibility. Use {@link eu.domibus.plugin.handler.MessageRetriever#getStatus(String)} instead
     *
     */
    @Deprecated
    MessageStatus getMessageStatus(String messageId);

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
