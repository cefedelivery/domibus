package eu.domibus.pki;

/**
 * Created by Cosmin Baciu on 06-Jul-16.
 */
public class DomibusCRLException extends RuntimeException {

    public DomibusCRLException() {
    }

    public DomibusCRLException(String message) {
        super(message);
    }

    public DomibusCRLException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomibusCRLException(Throwable cause) {
        super(cause);
    }

    public DomibusCRLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
