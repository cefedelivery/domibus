package eu.domibus.pki;

/**
 * Created by Cosmin Baciu on 06-Jul-16.
 */
public class DomibusCertificateRevokedException extends RuntimeException {

    public DomibusCertificateRevokedException() {
    }

    public DomibusCertificateRevokedException(String message) {
        super(message);
    }

    public DomibusCertificateRevokedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomibusCertificateRevokedException(Throwable cause) {
        super(cause);
    }

    public DomibusCertificateRevokedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
