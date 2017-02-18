package eu.domibus.common;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class MessageReceiveFailureEvent {

    protected String messageId;
    protected String endpoint;
    protected ErrorResult errorResult;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public ErrorResult getErrorResult() {
        return errorResult;
    }

    public void setErrorResult(ErrorResult errorResult) {
        this.errorResult = errorResult;
    }
}
