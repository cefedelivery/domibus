package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class that stores Schema information
 *
 * It stores information about Schema
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class SchemaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Schema Location {@link String}
     */
    private String location;

    /**
     * Schema Version {@link String}
     */
    private String version;

    /**
     * Schema Namespace {@link String}
     */
    private String namespace;

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
    public String toString() {
        return new ToStringBuilder(this)
                .append("location", location)
                .append("version", version)
                .append("namespace", namespace)
                .toString();
    }
}
