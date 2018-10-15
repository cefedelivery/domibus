package eu.domibus.core.logging;

import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResponseRO;

import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public interface LoggingService {

    /**
     * Set the logging level for the given package or class
     *
     * @param loggingLevelRO
     * @return
     */
    LoggingLevelResponseRO setLoggingLevel(LoggingLevelRO loggingLevelRO);

    /**
     * Get the logging levels for the given packages which starts or contains with {@code name} parameter
     *
     * @param name
     * @param showClasses
     * @return
     */
    List<LoggingLevelRO> getLoggingLevel(final String name, final boolean showClasses);
}
