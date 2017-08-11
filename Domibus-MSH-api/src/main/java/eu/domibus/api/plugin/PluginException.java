package eu.domibus.api.plugin;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class PluginException extends DomibusCoreException {
    public PluginException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public PluginException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }
}
