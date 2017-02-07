package eu.domibus.ext.services.exceptions;

/**
 * Raised in case an exception occurs when dealing with acknowledgment of messages
 */
public class MessageAcknowledgeException extends DomibusServiceException {

    public MessageAcknowledgeException(DomibusError domErr) {
        super(domErr);
    }
}