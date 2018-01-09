package eu.domibus.plugin.jms;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class NoMactchingAliasException extends RuntimeException {
    public NoMactchingAliasException(String message) {
        super(message);
    }
}
