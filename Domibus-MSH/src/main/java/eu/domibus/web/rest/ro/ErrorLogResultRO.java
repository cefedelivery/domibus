package eu.domibus.web.rest.ro;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class ErrorLogResultRO implements Serializable {

    private Map<String, Object> filter; //NOSONAR
    private List<ErrorLogRO> errorLogEntries;

    private MSHRole[] mshRoles;
    private ErrorCode[] errorCodes;

    private Integer count;
    private Integer page;
    private Integer pageSize;

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

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
