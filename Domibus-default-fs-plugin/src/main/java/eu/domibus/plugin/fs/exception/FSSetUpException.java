package eu.domibus.plugin.fs.exception;

/**
 * FSSetUpException
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSSetUpException extends FSPluginException {

    /**
     * Creates a new <code>FSSetUpException/code>.
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public FSSetUpException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>FSSetUpException</code>.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public FSSetUpException(String message, Throwable cause) {
        super(message, cause);
    }

}
