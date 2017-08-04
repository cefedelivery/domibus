package eu.domibus.plugin.fs.exception;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPayloadException extends Exception {

    public FSPayloadException() {
    }

    public FSPayloadException(String msg) {
        super(msg);
    }

    public FSPayloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FSPayloadException(Throwable cause) {
        super(cause);
    }
    
}
