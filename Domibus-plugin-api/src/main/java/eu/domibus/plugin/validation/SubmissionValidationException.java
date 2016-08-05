package eu.domibus.plugin.validation;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public class SubmissionValidationException extends RuntimeException {

    public SubmissionValidationException() {
    }

    public SubmissionValidationException(String message) {
        super(message);
    }

    public SubmissionValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubmissionValidationException(Throwable cause) {
        super(cause);
    }
}
