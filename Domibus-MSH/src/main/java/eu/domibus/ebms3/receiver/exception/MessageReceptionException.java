
package eu.domibus.ebms3.receiver.exception;

/**
 * @author Christian Koch, Stefan Mueller
 */

public class MessageReceptionException extends Exception {

    public MessageReceptionException() {
    }

    public MessageReceptionException(final String message) {
        super(message);
    }

    public MessageReceptionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MessageReceptionException(final Throwable cause) {
        super(cause);
    }

    public MessageReceptionException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
