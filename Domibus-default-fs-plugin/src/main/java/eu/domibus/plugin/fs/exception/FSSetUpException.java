package eu.domibus.plugin.fs.exception;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSSetUpException extends Exception {

    public FSSetUpException() {
    }

    public FSSetUpException(String msg) {
        super(msg);
    }

    public FSSetUpException(String message, Throwable cause) {
        super(message, cause);
    }

    public FSSetUpException(Throwable cause) {
        super(cause);
    }
    
}
