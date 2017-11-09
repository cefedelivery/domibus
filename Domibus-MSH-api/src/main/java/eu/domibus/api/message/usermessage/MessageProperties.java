package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Set;

public class MessageProperties {

    private Set<Property> property;

    public Set<Property> getProperty() {
        return property;
    }

    public void setProperty(Set<Property> property) {
        this.property = property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageProperties that = (MessageProperties) o;

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
