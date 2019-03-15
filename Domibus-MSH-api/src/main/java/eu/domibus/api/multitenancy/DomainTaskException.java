package eu.domibus.api.multitenancy;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomainTaskException extends DomibusCoreException {

    public DomainTaskException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomainTaskException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomainTaskException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomainTaskException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public DomainTaskException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
