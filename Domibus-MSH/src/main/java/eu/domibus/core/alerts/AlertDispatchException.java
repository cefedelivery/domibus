package eu.domibus.core.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertDispatchException extends RuntimeException{

    private final static Logger LOG = LoggerFactory.getLogger(AlertDispatchException.class);

    public AlertDispatchException(Throwable cause) {
        super(cause);
    }
}
