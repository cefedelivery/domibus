package eu.domibus.api.exceptions;

/**
 * This class is the root exceptions for Domibus core errors.
 * <p>
 * <p>It provides two constructors using the enum DomibusCoreErrorCode, one accepts the throwable cause.
 *
 * @author Federico Martini
 * @see DomibusCoreErrorCode
 * <p>
 * //TODO change package when refactoring of MSH will take place.
 * @since 3.3
 */
public class DomibusCoreException extends RuntimeException {

    private DomibusCoreErrorCode error;

    /**
     * Constructs a new DomibusCoreException with a specific error and message.
     *
     * @param dce     a DomibusCoreErrorCode.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     */
    public DomibusCoreException(DomibusCoreErrorCode dce, String message) {
        super("[" + dce + "]:" + message);
        error = dce;
    }

    /**
     * Constructs a new DomibusCoreException with a specific error, message and cause.
     *
     * @param dce     a DomibusCoreErrorCode.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause of the exceptions.
     */
    public DomibusCoreException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super("[" + dce + "]:" + message, cause);
        error = dce;
    }

    public DomibusCoreErrorCode getError() {
        return error;
    }

    public void setError(DomibusCoreErrorCode error) {
        this.error = error;
    }


}
