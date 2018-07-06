package eu.domibus.core.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AlertDispatchException extends RuntimeException{

    public AlertDispatchException(Throwable cause) {
        super(cause);
    }
}
