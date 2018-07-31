package eu.domibus.api.csv;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

public class CsvException extends DomibusCoreException {

    public CsvException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }
}
