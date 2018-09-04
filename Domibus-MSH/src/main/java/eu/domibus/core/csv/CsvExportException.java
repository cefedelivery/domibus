package eu.domibus.core.csv;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Exception to map CSV export operation
 */
public class CsvExportException extends DomibusCoreException {


    public CsvExportException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public CsvExportException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
