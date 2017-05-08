package eu.domibus.api.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class UserMessageException extends DomibusCoreException {

    public UserMessageException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public UserMessageException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public UserMessageException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
