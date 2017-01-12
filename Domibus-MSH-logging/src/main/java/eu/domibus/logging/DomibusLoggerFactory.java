package eu.domibus.logging;

import org.slf4j.LoggerFactory;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLoggerFactory {

    private DomibusLoggerFactory() {}

    public static DomibusLogger getLogger(String name) {
        return new DomibusLogger(LoggerFactory.getLogger(name));
    }

    public static DomibusLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}
