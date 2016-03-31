package eu.domibus.messaging;

/**
 * This exception indicates that a message with the requested id could not be found on the MSH. The reason might be that
 * the message does not exist at all or that it is associated with a different backend plugin.
 *
 * @author Christian Koch, Stefan Mueller
 */


public class MessageNotFoundException extends MessagingProcessingException {

    public MessageNotFoundException(String message) {
        super(message);
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MessageNotFoundException() {
    }

    public MessageNotFoundException(Throwable cause) {
        super(cause);
    }
}
