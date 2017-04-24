package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity(name = "CONFIGURATION_PROPERTY")
@Table(name = "TB_MESSAGE_PROPERTY")
public class Property extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "key", required = true)
    @Column(name = "KEY_")
    protected String key;
    @XmlAttribute(name = "datatype", required = true)
    @Column(name = "DATATYPE")
    protected String datatype;
    @XmlAttribute(name = "required", required = true)
    @Column(name = "REQUIRED_")
    protected boolean required;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;
        if (!super.equals(o)) return false;

        final Property property = (Property) o;

        if (this.required != property.required) return false;
        if (!this.name.equals(property.name)) return false;
        if (!this.key.equals(property.key)) return false;
        return !(this.datatype != null ? !this.datatype.equals(property.datatype) : property.datatype != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.name.hashCode();
        result = 31 * result + this.key.hashCode();
        result = 31 * result + (this.datatype != null ? this.datatype.hashCode() : 0);
        result = 31 * result + (this.required ? 1 : 0);
        return result;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * <p/>
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

    /**
     * Gets the value of the key property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Sets the value of the key property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKey(final String value) {
        this.key = value;
    }

    /**
     * Gets the value of the datatype property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDatatype() {
        return this.datatype;
    }

    /**
     * Sets the value of the datatype property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDatatype(final String value) {
        this.datatype = value;
    }

    /**
     * Gets the value of the required property.
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Sets the value of the required property.
     */
    public void setRequired(final boolean value) {
        this.required = value;
    }

    public void init(final Configuration configuration) {

    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + this.name + '\'' +
                ", key='" + this.key + '\'' +
                ", datatype='" + this.datatype + '\'' +
                ", required=" + this.required +
                '}';
    }
}
