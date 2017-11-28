package eu.domibus.web.rest.ro;

import eu.domibus.api.routing.RoutingCriteria;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageFilterRO implements Serializable {

    private int entityId;

    private int index;

    private List<RoutingCriteria> routingCriterias;

    private String backendName;

    private boolean isPersisted;

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

    public boolean isPersisted() {
        return isPersisted;
    }

    public void setPersisted(boolean isPersisted) {
        this.isPersisted = isPersisted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageFilterRO that = (MessageFilterRO) o;

        return new EqualsBuilder()
                .append(entityId, that.entityId)
                .append(index, that.index)
                .append(isPersisted, that.isPersisted)
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
                .append(isPersisted)
                .toHashCode();
    }

    private String routingCriteriasToCsvString() {
        // I don't like this approach but we have a fixed table for Routing Criterias and we need to keep this order always
        // even if routing criterias exist on a different order in backend filter object
        String[] result = {"","","",""};
        for(RoutingCriteria rc : routingCriterias) {
            if(rc.getName().equalsIgnoreCase("from")) {
                result[0] = Objects.toString(rc.getExpression(),"");
            }
            if(rc.getName().equalsIgnoreCase("to")) {
                result[1] = Objects.toString(rc.getExpression(),"");
            }
            if(rc.getName().equalsIgnoreCase("action")) {
                result[2] = Objects.toString(rc.getExpression(),"");
            }
            if(rc.getName().equalsIgnoreCase("service")) {
                result[3] = Objects.toString(rc.getExpression(),"");
            }
        }
        return Arrays.toString(result).replace("[","").replace("]", "");
    }

    public String toCsvString() {
        return new StringBuilder()
                .append(Objects.toString(backendName,"")).append(",")
                .append(routingCriteriasToCsvString()).append(",")
                .append(Objects.toString(isPersisted, ""))
                .append(System.lineSeparator())
                .toString();
    }

    public static String csvTitle() {
        return new StringBuilder()
                .append("Backend Name").append(",")
                .append("From").append(",")
                .append("To").append(",")
                .append("Action").append(",")
                .append("Service").append(",")
                .append("Persisted")
                .append(System.lineSeparator())
                .toString();
    }
}
