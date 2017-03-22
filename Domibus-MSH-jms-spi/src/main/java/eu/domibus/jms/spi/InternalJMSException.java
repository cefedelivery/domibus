package eu.domibus.jms.spi;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class InternalJMSException extends RuntimeException {

    public InternalJMSException() {
    }

    public InternalJMSException(String message) {
        super(message);
    }

    public InternalJMSException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalJMSException(Throwable cause) {
        super(cause);
    }
}
