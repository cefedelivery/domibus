package eu.domibus.ext.services.exceptions;

/**
 * Specific exception to report errors occurring in the Messages Monitor Service.
 *
 * @author Federico Martini
 * @since 3.3
 */
public class MessagesMonitorException extends DomibusServiceException {

    public MessagesMonitorException(DomibusError domErr) {
        super(domErr);
    }

}
