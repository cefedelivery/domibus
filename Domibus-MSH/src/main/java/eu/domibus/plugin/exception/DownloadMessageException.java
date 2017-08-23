
package eu.domibus.plugin.exception;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class DownloadMessageException extends Exception {
    public DownloadMessageException(final String s) {
        super(s);
    }

    public DownloadMessageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DownloadMessageException(final Throwable cause) {
        super(cause);
    }

    public DownloadMessageException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DownloadMessageException() {
    }
}
