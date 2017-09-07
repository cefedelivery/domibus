package eu.domibus.api.jms;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusJMSException extends DomibusCoreException {

    public DomibusJMSException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomibusJMSException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomibusJMSException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }

}
