package eu.domibus.api.user;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class UserManagementException extends DomibusCoreException {

    public UserManagementException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public UserManagementException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
