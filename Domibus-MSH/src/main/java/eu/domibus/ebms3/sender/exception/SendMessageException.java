
package eu.domibus.ebms3.sender.exception;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class SendMessageException extends RuntimeException {

    public SendMessageException() {
    }

    public SendMessageException(final String message) {
        super(message);
    }

    public SendMessageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SendMessageException(final Throwable cause) {
        super(cause);
    }

    public SendMessageException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
