package eu.domibus.core.crypto.spi;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Equivalent of CryptoException within the IAM module.
 * Will be transformed to CryptoException in the core.
 */
public class CryptoSpiException extends RuntimeException {

    public CryptoSpiException(String message) {
        super(message);
    }

    public CryptoSpiException(Throwable cause) {
        super(cause);
    }

    public CryptoSpiException(String message, Throwable cause) {
        super(message, cause);
    }
}
