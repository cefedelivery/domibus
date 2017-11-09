package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Schema {

    private String location;

    private String version;

    private String namespace;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNamespace() {
        return namespace;
    }

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
