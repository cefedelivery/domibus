package eu.domibus.api.security;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class ChainCertificateInvalidException extends DomibusCoreException {
    public ChainCertificateInvalidException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public ChainCertificateInvalidException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }
}
