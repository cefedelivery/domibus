package eu.domibus.api.reliability;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class ReliabilityException extends DomibusCoreException {

    public ReliabilityException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }
    public ReliabilityException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

}
