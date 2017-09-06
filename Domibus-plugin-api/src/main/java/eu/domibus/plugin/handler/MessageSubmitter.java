
package eu.domibus.plugin.handler;


import eu.domibus.messaging.MessagingProcessingException;

/**
 * Implementations of this interface handle the plugin of messages from the
 * backend to holodeck.
 *
 * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 4.0
 *
 * @param <T> Data transfer object
 *            (http://en.wikipedia.org/wiki/Data_transfer_object) transported between the
 *            backend and Domibus
 * @author Christian Koch, Stefan Mueller
 */
@Deprecated
public interface MessageSubmitter<T> {

    /**
     * Submits a message to Domibus to be processed.
     *
     * @param messageData the message to be processed
     * @return the messageId of the submitted message
     *
     * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 4.0
     */
    @Deprecated
    public String submit(T messageData, String submitterName) throws MessagingProcessingException;
}
