package eu.domibus.plugin.fs.exception;

/**
 * FSPayloadException
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPayloadException extends FSPluginException {


    /**
     * Creates a new <code>FSPayloadException/code>.
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public FSPayloadException(String message) {
        super(message);
    }


    /**
     * Creates a new <code>FSPayloadException</code>.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public FSPayloadException(String message, Throwable cause) {
        super(message, cause);
    }

}
