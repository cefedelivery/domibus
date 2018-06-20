package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class that stores Property details
 *
 * It stores information about Property
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class PropertyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Property Value {@link String}
     */
    private String value;

    /**
     * Property Name {@link String}
     */
    private String name;

    /**
     * Property Type {@link String}
     */
    private String type;

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
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("name", name)
                .append("type", type)
                .toString();
    }
}
