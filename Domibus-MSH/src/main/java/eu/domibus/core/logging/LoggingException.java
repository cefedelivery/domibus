package eu.domibus.core.logging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Encapsulates any error related to setting or getting log levels at runtime
 *
 * @author Catalin Enache
 * @since 4.1
 */
public class LoggingException extends DomibusCoreException {

    public LoggingException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public LoggingException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public LoggingException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public LoggingException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public LoggingException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
