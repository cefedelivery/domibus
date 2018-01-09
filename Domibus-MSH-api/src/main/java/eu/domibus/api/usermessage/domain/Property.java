package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class Property {

    /**
     * Property Value {@link String}
     */
    protected String value;

    /**
     * Property Name {@link String}
     */
    protected String name;

    /**
     * Property Type {@link String}
     */
    protected String type;

    /**
     * Gets the Property Value
     * @return Property Value {@link String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the Property Value
     * @param value Property Value {@link String}
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the Property Name
     * @return Property Name {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the Property Name
     * @param name Property Name {@link String}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets Property Type
     * @return Property Type {@link String}
     */
    public String getType() {
        return type;
    }

    /**
     * Sets Property Type
     * @param type Property Type {@link String}
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Property property = (Property) o;

        return new EqualsBuilder()
                .append(value, property.value)
                .append(name, property.name)
                .append(type, property.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(name)
                .append(type)
                .toHashCode();
    }
}
