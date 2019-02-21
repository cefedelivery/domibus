package eu.domibus.core.crypto.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class CryptoSpiException extends RuntimeException {

    public CryptoSpiException(String message) {
        super(message);
    }

    public CryptoSpiException(String message, Throwable cause) {
        super(message, cause);
    }
}
