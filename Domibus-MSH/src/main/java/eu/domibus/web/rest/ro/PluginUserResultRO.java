package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;

/**
 * @author Pion
 * @since 4.0
 */
public class PluginUserResultRO implements Serializable {

    private List<PluginUserRO> entries;
    private Long count;
    private Integer page;
    private Integer pageSize;

    public List<PluginUserRO> getEntries() {
        return entries;
    }

    public void setEntries(List<PluginUserRO> entries) {
        this.entries = entries;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
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

}
