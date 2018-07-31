package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class Schema {

    /**
     * Schema Location {@link String}
     */
    protected String location;

    /**
     * Schema Version {@link String}
     */
    protected String version;

    /**
     * Schema Namespace {@link String}
     */
    protected String namespace;

    /**
     * Gets Schema Location
     * @return Schema Location {@link String}
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets Schema Location
     * @param location Schema Location {@link String}
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets Schema Version
     * @return Schema Version {@link String}
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets Schema Version
     * @param version Schema Version {@link String}
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets Schema Namespace
     * @return Schema Namespace {@link String}
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets Schema Namespace
     * @param namespace Schema Namespace {@link String}
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Schema schema = (Schema) o;

        return new EqualsBuilder()
                .append(location, schema.location)
                .append(version, schema.version)
                .append(namespace, schema.namespace)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(location)
                .append(version)
                .append(namespace)
                .toHashCode();
    }
}
