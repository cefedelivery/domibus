
package eu.domibus.common.model.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="legConfiguration" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="reliability" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="security" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="receptionAwareness" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="action" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="mpc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="propertySet" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="payloadProfile" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="errorHandling" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="compressPayloads" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="splitting" type="{http://domibus.eu/configuration}max255-non-empty-string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "legConfiguration")
public class LegConfigurations {

    @XmlElement(required = true)
    protected List<LegConfiguration> legConfiguration;

    /**
     * Gets the value of the legConfiguration property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the legConfiguration property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLegConfiguration().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LegConfiguration }
     *
     * @return a reference to the live list of Leg configurations
     */
    public List<LegConfiguration> getLegConfiguration() {
        if (this.legConfiguration == null) {
            this.legConfiguration = new ArrayList<>();
        }
        return this.legConfiguration;
    }

}
