
package eu.domibus.common.model.configuration;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="party" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="mpc" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class PartyMpc {

    @XmlAttribute(name = "party")
    @XmlSchemaType(name = "anySimpleType")
    protected String party;
    @XmlAttribute(name = "mpc")
    @XmlSchemaType(name = "anySimpleType")
    protected String mpc;

    /**
     * Gets the value of the party property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getParty() {
        return this.party;
    }

    /**
     * Sets the value of the party property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setParty(final String value) {
        this.party = value;
    }

    /**
     * Gets the value of the mpc property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMpc() {
        return this.mpc;
    }

    /**
     * Sets the value of the mpc property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMpc(final String value) {
        this.mpc = value;
    }

}
