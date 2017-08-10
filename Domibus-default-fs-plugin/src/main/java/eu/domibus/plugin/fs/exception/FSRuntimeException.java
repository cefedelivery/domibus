package eu.domibus.plugin.fs.exception;

/**
 * FSRuntimeException
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSRuntimeException extends RuntimeException {

    /**
     * Creates a new <code>FSRuntimeException</code>.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public FSRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
