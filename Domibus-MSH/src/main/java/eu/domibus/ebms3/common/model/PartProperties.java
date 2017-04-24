package eu.domibus.ebms3.common.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * This element has zero or more eb:Property child elements. An eb:Property element is of
 * xs:anySimpleType (e.g. string, URI) and has a REQUIRED @name attribute, the value of which
 * must be agreed between partners. Its actual semantics is beyond the scope of this specification.
 * The element is intended to be consumed outside the ebMS specified functions. It may contain
 * meta-data that qualifies or abstracts the payload data. A representation in the header of such
 * properties allows for more efficient monitoring, correlating, dispatching and validating functions
 * (even if these are out of scope of ebMS specification) that do not require payload access.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartProperties", propOrder = "property")
@Embeddable
public class PartProperties {

    @XmlElement(name = "Property", required = true)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "PARTPROPERTIES_ID")
    protected Set<Property> property;

    /**
     * Gets the value of the property property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the property property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     */
    public Set<Property> getProperties() {
        if (this.property == null) {
            this.property = new HashSet<>();
        }
        return this.property;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("property", property)
                .toString();
    }

}
