package eu.domibus.ext.api.v2.exception;

public class MessageAcknowledgeExceptionExt extends RuntimeException {

    public MessageAcknowledgeExceptionExt() {
        super();
    }

    public MessageAcknowledgeExceptionExt(String message) {
        super(message);
    }

    public MessageAcknowledgeExceptionExt(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageAcknowledgeExceptionExt(Throwable cause) {
        super(cause);
    }

    protected MessageAcknowledgeExceptionExt(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}