
package eu.domibus.plugin.validation.exception;

/**
 * Indicates a violation of a validation rule that occured while validating a
 * message
 *
 * @author Christian Koch, Stefan Mueller
 * @see eu.domibus.plugin.validation.Validator
 */
public class ValidationException extends Exception {

    public ValidationException() {
    }


    public ValidationException(final String message) {
        super(message);
    }

    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ValidationException(final Throwable cause) {
        super(cause);
    }
}
