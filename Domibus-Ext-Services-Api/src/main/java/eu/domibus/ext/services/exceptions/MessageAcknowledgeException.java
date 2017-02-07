package eu.domibus.ext.services.exceptions;

/**
 * Raised in case an exception occurs when dealing with message acknowledgments.
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgeException extends DomibusServiceException {

    public MessageAcknowledgeException(DomibusError domErr) {
        super(domErr);
    }
}