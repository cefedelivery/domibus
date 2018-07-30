
package eu.domibus.submission.validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="element1" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="element2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "element1",
    "element2"
})
@XmlRootElement(name = "payload")
public class Payload {

    @XmlElement(required = true)
    protected byte[] element1;
    @XmlElement(required = true)
    protected String element2;

    /**
     * Gets the value of the element1 property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getElement1() {
        return element1;
    }

    /**
     * Sets the value of the element1 property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setElement1(byte[] value) {
        this.element1 = value;
    }

    /**
     * Gets the value of the element2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElement2() {
        return element2;
    }

    /**
     * Sets the value of the element2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElement2(String value) {
        this.element2 = value;
    }

}
