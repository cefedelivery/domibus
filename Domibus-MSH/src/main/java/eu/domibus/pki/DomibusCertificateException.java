package eu.domibus.pki;

/**
 * Created by Cosmin Baciu on 06-Jul-16.
 */
public class DomibusCertificateException extends RuntimeException {

    public DomibusCertificateException() {
    }

    public DomibusCertificateException(String message) {
        super(message);
    }

    public DomibusCertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomibusCertificateException(Throwable cause) {
        super(cause);
    }

    public DomibusCertificateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
