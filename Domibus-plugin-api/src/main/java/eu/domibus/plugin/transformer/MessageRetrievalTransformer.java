

package eu.domibus.plugin.transformer;
import eu.domibus.plugin.Submission;

/**
 * Implementations of this interface transform a message of type
 * {@link eu.domibus.plugin.Submission} to an object of type {@literal <T>}
 *
 * @param <U> Data transfer object
 *            (http://en.wikipedia.org/wiki/Data_transfer_object) transported between the
 *            backend and Domibus
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public interface MessageRetrievalTransformer<U> {

    /**
     * transforms the Messaging to the typed object
     *
     * @param submission the {@link eu.domibus.plugin.Submission} to be transformed
     * @param target the output target
     * @return the transformed message
     */
    U transformFromSubmission(Submission submission, U target);
}
