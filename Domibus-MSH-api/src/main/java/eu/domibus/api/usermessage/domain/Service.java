package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class Service {

    /**
     * Service Value {@link String}
     */
    protected String value;

    /**
     * Service Type {@link String}
     */
    protected String type;

    /**
     * Gets Service Value
     * @return Service Value {@link String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets Service Value
     * @param value Service Value {@link String}
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets Service Type
     * @return Service Type {@link String}
     */
    public String getType() {
        return type;
    }

    /**
     * Sets Service Type
     * @param type Service Type {@link String}
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        return new EqualsBuilder()
                .append(value, service.value)
                .append(type, service.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(type)
                .toHashCode();
    }
}
