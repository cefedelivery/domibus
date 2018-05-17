package eu.domibus.api.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class CryptoException extends DomibusCoreException {

    public CryptoException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public CryptoException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public CryptoException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public CryptoException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public CryptoException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
