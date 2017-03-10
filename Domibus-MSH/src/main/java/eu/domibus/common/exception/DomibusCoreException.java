package eu.domibus.common.exception;

/**
 * This class is the root exception for Domibus core errors.
 * <p>
 * <p>It provides two constructors using the enum DomibusCoreError, one accepts the throwable cause.
 *
 * @author Federico Martini
 * @see DomibusCoreError
 * <p>
 * //TODO change package when refactoring of MSH will take place.
 * @since 3.3
 */
public class DomibusCoreException extends RuntimeException {

    private DomibusCoreError error;

    /**
     * Constructs a new DomibusCoreException with a specific error and message.
     *
     * @param dce     a DomibusCoreError.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     */
    public DomibusCoreException(DomibusCoreError dce, String message) {
        super("[" + dce + "]:" + message);
        error = dce;
    }

    /**
     * Constructs a new DomibusCoreException with a specific error, message and cause.
     *
     * @param dce     a DomibusCoreError.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause of the exception.
     */
    public DomibusCoreException(DomibusCoreError dce, String message, Throwable cause) {
        super("[" + dce + "]:" + message);
        error = dce;
    }

    public DomibusCoreError getError() {
        return error;
    }

    public void setError(DomibusCoreError error) {
        this.error = error;
    }


}
