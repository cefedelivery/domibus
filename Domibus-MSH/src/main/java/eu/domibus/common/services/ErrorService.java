package eu.domibus.common.services;

import eu.domibus.common.model.logging.ErrorLogEntry;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ErrorService {
    /**
     * persist an error log.
     *
     * @param errorLogEntry the error.
     */
    void createErrorLog(ErrorLogEntry errorLogEntry);
}
