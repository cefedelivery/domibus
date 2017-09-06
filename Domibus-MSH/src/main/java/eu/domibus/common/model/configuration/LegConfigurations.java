
package eu.domibus.common.model.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="legConfiguration" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="reliability" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="security" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="receptionAwareness" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="action" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="mpc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="propertySet" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="payloadProfile" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="errorHandling" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "legConfiguration")
public class LegConfigurations {

    @XmlElement(required = true)
    protected List<LegConfiguration> legConfiguration;

    /**
     * Gets the value of the legConfiguration property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the legConfiguration property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLegConfiguration().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link LegConfiguration }
     */
    public List<LegConfiguration> getLegConfiguration() {
        if (this.legConfiguration == null) {
            this.legConfiguration = new ArrayList<>();
        }
        return this.legConfiguration;
    }

}
