package eu.domibus.ext.rest;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class FailedMessagesCriteriaRO implements Serializable {

    private Date fromDate;
    private Date toDate;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}
