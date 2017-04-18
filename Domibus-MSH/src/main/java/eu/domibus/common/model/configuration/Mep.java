package eu.domibus.common.model.configuration;

import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.*;
import java.math.BigInteger;

/**
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_MEP")
public class Mep extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "value", required = true)
    @XmlSchemaType(name = "anyURI")
    @Column(name = "VALUE")
    protected String value;
    @XmlAttribute(name = "legs")
    @Column(name = "LEG_COUNT")
    protected int legs;

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

    /**
     * Gets the value of the value property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the value of the legs property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public int getLegs() {

        return this.legs;

    }

    /**
     * Sets the value of the legs property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setLegs(final int value) {
        this.legs = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Mep)) return false;
        if (!super.equals(o)) return false;

        final Mep mep = (Mep) o;

        if (!name.equals(mep.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public void init(final Configuration configuration) {

    }
}
