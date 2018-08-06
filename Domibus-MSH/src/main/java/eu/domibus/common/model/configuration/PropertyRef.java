
package eu.domibus.common.model.configuration;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="property" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class PropertyRef {

    @XmlAttribute(name = "property")
    @XmlSchemaType(name = "anySimpleType")
    protected String property;

    /**
     * Gets the value of the property property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProperty() {
        return this.property;
    }

    /**
     * Sets the value of the property property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProperty(final String value) {
        this.property = value;
    }
}
