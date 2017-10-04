package eu.domibus.api.routing;

import java.io.Serializable;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class RoutingCriteria implements Serializable {

    private int entityId;

    private String name;

    private String expression;

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
