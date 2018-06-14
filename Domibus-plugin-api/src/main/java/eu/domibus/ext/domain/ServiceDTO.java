package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class that stores Service details
 *
 * It stores information about Service
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class ServiceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Service Value {@link String}
     */
    private String value;

    /**
     * Service Type {@link String}
     */
    private String type;

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
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("type", type)
                .toString();
    }
}
