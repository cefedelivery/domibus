package eu.domibus.api.routing;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class BackendFilter implements Serializable, Comparable {

    private int entityId;

    private int index;

    private List<RoutingCriteria> routingCriterias;

    private String backendName;

    private boolean active;

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<RoutingCriteria> getRoutingCriterias() {
        return routingCriterias;
    }

    public void setRoutingCriterias(List<RoutingCriteria> routingCriterias) {
        this.routingCriterias = routingCriterias;
    }

    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(Object o) {
        return this.entityId-((BackendFilter)o).getEntityId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BackendFilter that = (BackendFilter) o;

        return new EqualsBuilder()
                .append(entityId, that.entityId)
                .append(index, that.index)
                .append(active, that.active)
                .append(routingCriterias, that.routingCriterias)
                .append(backendName, that.backendName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(entityId)
                .append(index)
                .append(routingCriterias)
                .append(backendName)
                .append(active)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("entityId", entityId)
                .append("index", index)
                .append("routingCriterias", routingCriterias)
                .append("backendName", backendName)
                .append("active", active)
                .toString();
    }
}
