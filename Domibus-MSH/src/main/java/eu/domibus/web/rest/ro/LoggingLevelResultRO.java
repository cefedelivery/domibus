package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Response POJO for GET {@link eu.domibus.web.rest.LoggingResource} method
 * It keeps logging level entries as well the pagination info
 *
 * @author Catalin Enache
 * @since 4.1
 */
public class LoggingLevelResultRO implements Serializable {

    private List<LoggingLevelRO> loggingEntries;

    private Integer count;
    private Integer page;
    private Integer pageSize;

    private Map<String, Object> filter; //NOSONAR

    public List<LoggingLevelRO> getLoggingEntries() {
        return loggingEntries;
    }

    public void setLoggingEntries(List<LoggingLevelRO> loggingEntries) {
        this.loggingEntries = loggingEntries;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
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

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }
}
