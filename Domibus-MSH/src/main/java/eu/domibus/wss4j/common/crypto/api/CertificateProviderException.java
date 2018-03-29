package eu.domibus.wss4j.common.crypto.api;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class CertificateProviderException extends DomibusCoreException {

    public CertificateProviderException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public CertificateProviderException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public CertificateProviderException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
