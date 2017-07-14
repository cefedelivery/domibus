package eu.domibus.api.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageGenericException extends DomibusCoreException {

    public MessageGenericException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }
}
