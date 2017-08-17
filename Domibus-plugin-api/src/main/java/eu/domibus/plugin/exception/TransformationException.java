
package eu.domibus.plugin.exception;

import eu.domibus.messaging.MessagingProcessingException;

/**
 * This exception indicates an error during message transformation, i.e. missing mandatory parameters.
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

public class TransformationException extends MessagingProcessingException {

    public TransformationException() {
    }


    public TransformationException(final String message) {
        super(message);
    }


    public TransformationException(final String message, final Throwable cause) {
        super(message, cause);
    }


    public TransformationException(final Throwable cause) {
        super(cause);
    }

    public TransformationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
