package eu.domibus.api.routing;

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
}
