
package eu.domibus.messaging;

import eu.domibus.common.ErrorCode;

/**
 * This exception indicates that a message is not corresponding to its associated PMode and thus cannot be processed.
 *
 * @author Christian Koch, Stefan Mueller
 */
public class PModeMismatchException extends MessagingProcessingException {
    public PModeMismatchException() {
    }

    public PModeMismatchException(Throwable cause) {
        super(cause);
    }

    public PModeMismatchException(String message) {
        super(message);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0010);
    }

    public PModeMismatchException(String message, Throwable cause) {
        super(message, cause);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0010);
    }

    public PModeMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0010);
    }
}
