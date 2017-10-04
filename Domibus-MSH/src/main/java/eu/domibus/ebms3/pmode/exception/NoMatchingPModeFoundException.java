
package eu.domibus.ebms3.pmode.exception;

/**
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

public class NoMatchingPModeFoundException extends Exception {

    private final String messageId;

    public NoMatchingPModeFoundException(final String messageId) {
        super();
        this.messageId = messageId;
    }

    public String getMessageId() {
        return this.messageId;
    }
}
