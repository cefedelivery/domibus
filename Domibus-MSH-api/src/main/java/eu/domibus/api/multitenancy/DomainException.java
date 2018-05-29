package eu.domibus.api.multitenancy;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomainException extends DomibusCoreException {

    public DomainException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomainException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomainException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomainException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public DomainException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
