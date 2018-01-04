package eu.domibus.api.security;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class CertificateException extends DomibusCoreException {

    public CertificateException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

}
