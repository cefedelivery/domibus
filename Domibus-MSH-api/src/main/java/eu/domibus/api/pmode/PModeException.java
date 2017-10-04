package eu.domibus.api.pmode;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class PModeException extends DomibusCoreException {

    public PModeException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public PModeException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public PModeException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
