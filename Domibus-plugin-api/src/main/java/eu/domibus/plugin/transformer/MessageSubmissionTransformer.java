
package eu.domibus.plugin.transformer;

import eu.domibus.plugin.Submission;

/**
 * Implementations of this interface transform a message of type {@literal <T>}
 * to an object of type {@link eu.domibus.plugin.Submission}
 *
 * @param <T> Data transfer object
 *            (http://en.wikipedia.org/wiki/Data_transfer_object) transported between the
 *            backend and Domibus
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

public interface MessageSubmissionTransformer<T> {

    /**
     * transforms the typed object to an EbMessage
     *
     * @param messageData the message to be transformed
     * @return the transformed message
     */
    Submission transformToSubmission(T messageData);
}
