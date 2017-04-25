package eu.domibus.api.message.acknowledge;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgeException extends DomibusCoreException {

    public MessageAcknowledgeException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public MessageAcknowledgeException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public MessageAcknowledgeException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
