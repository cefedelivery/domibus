package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_ACTION")
@NamedQueries({@NamedQuery(name = "Action.findByAction", query = "select a.name from Action a where a.value = :ACTION"), @NamedQuery(name = "Action.findByName", query = "select a from Action a where a.name=:NAME")})
public class Action extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "value", required = true)
    @Column(name = "VALUE")
    protected String value;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;
        if (!super.equals(o)) return false;

        final Action action = (Action) o;

        if (!name.equals(action.name)) return false;
        if (!value.equals(action.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
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

    public void init(final Configuration configuration) {

    }
}
