package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class PartProperties {

    /**
     * {@link Set} of Properties {@link Property}
     */
    protected Set<Property> property;

    /**
     * Gets the Set of Properties
     * @return {@link Set} of Properties {@link Property}
     */
    public Set<Property> getProperty() {
        return property;
    }

    /**
     * Sets the Set of Properties
     * @param property {@link Set} of Properties {@link Property}
     */
    public void setProperty(Set<Property> property) {
        this.property = property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartProperties that = (PartProperties) o;

        return new EqualsBuilder()
                .append(property, that.property)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(property)
                .toHashCode();
    }
}
