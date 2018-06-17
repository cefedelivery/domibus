package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO class for Part Properties information
 *
 * It stores information about Part Properties
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class PartPropertiesDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * {@link Set} of Properties {@link PropertyDTO}
     */
    private Set<PropertyDTO> property;

    /**
     * Gets the Set of Properties
     * @return {@link Set} of Properties {@link PropertyDTO}
     */
    public Set<PropertyDTO> getProperty() {
        return property;
    }

    /**
     * Sets the Set of Properties
     * @param property {@link Set} of Properties {@link PropertyDTO}
     */
    public void setProperty(Set<PropertyDTO> property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("property", property)
                .toString();
    }
}
