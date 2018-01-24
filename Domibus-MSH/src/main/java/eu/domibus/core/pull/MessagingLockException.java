package eu.domibus.core.pull;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class MessagingLockException extends RuntimeException{

    Integer messageAlreadyLockedId;

    public MessagingLockException(Integer messageAlreadyLockedId) {
        this.messageAlreadyLockedId = messageAlreadyLockedId;
    }

    public MessagingLockException(String message) {
        super(message);
    }

    public Integer getMessageAlreadyLockedId() {
        return messageAlreadyLockedId;
    }
}
