package eu.domibus.plugin.jms;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class DefaultJmsPluginException extends RuntimeException {

    public DefaultJmsPluginException(Exception e) {
        super(e);
    }

    public DefaultJmsPluginException(String message, Exception e) {
        super(message, e);
    }

    public DefaultJmsPluginException(String message) {
        super(message);
    }
}
