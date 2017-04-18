package eu.domibus.common.model.configuration;

import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "propertyRef")
@Entity
@Table(name = "TB_MESSAGE_PROPERTY_SET")
public class PropertySet extends AbstractBaseEntity {

    @XmlAttribute(name = "name")
    @Column(name = "NAME")
    protected String name;

    @XmlElement(required = true, name = "propertyRef")
    @Transient
    protected List<PropertyRef> propertyRef;


    @XmlTransient
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TB_JOIN_PROPERTY_SET", joinColumns = @JoinColumn(name = "PROPERTY_FK"), inverseJoinColumns = @JoinColumn(name = "SET_FK"))
    private Set<Property> properties;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    public void init(final Configuration configuration) {
        this.properties = new HashSet<>();
        for (final PropertyRef ref : this.propertyRef) {
            for (final Property property : configuration.getBusinessProcesses().getProperties()) {
                if (ref.getProperty().equals(property.getName())) {
                    this.properties.add(property);
                    break;
                }
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertySet)) return false;
        if (!super.equals(o)) return false;

        final PropertySet that = (PropertySet) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public Set<Property> getProperties() {
        return this.properties;
    }
}
