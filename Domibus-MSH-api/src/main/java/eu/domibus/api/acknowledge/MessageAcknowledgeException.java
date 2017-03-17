package eu.domibus.api.acknowledge;

import eu.domibus.api.exceptions.DomibusCoreError;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Created by migueti on 15/03/2017.
 */
public class MessageAcknowledgeException extends DomibusCoreException {
    public MessageAcknowledgeException(DomibusCoreError dce, String message) {
        super(dce, message);
    }

    public MessageAcknowledgeException(DomibusCoreError dce, String message, Throwable cause) {
        super(dce, message, cause);
    }
}
