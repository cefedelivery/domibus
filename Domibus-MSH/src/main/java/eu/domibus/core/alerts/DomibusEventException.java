package eu.domibus.core.alerts;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomibusEventException extends DomibusCoreException {

    public DomibusEventException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomibusEventException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomibusEventException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
