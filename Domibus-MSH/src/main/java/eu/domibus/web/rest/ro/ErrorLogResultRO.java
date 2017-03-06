package eu.domibus.web.rest.ro;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class ErrorLogResultRO {

    private List<ErrorLogRO> errorLogEntries;
    private Integer count;
    private MSHRole[] mshRoles;
    private ErrorCode[] errorCodes;

    public List<ErrorLogRO> getErrorLogEntries() {
        return errorLogEntries;
    }

    public void setErrorLogEntries(List<ErrorLogRO> errorLogEntries) {
        this.errorLogEntries = errorLogEntries;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public MSHRole[] getMshRoles() {
        return mshRoles;
    }

    public void setMshRoles(MSHRole[] mshRoles) {
        this.mshRoles = mshRoles;
    }

    public ErrorCode[] getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(ErrorCode[] errorCodes) {
        this.errorCodes = errorCodes;
    }
}
