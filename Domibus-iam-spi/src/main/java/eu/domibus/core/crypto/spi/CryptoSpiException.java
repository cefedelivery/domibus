package eu.domibus.core.crypto.spi;

/**
 * @author Thomas Dussart
 * @since 4.1
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
