package eu.domibus.ebms3.common.model.mf;

import javax.xml.bind.annotation.*;


/**
 * Auto-generated JAXB class based on the SplitAndJoin XSD
 *
 * <p>Java class for MessageHeaderType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="MessageHeaderType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Content-Type" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}non-empty-string"/&gt;
 *         &lt;element name="Boundary" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}non-empty-string"/&gt;
 *         &lt;element name="Type" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}TypeType"/&gt;
 *         &lt;element name="Start" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}non-empty-string"/&gt;
 *         &lt;element name="StartInfo" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}non-empty-string" minOccurs="0"/&gt;
 *         &lt;element name="Content-Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageHeaderType", namespace = "http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/", propOrder = {
        "contentType",
        "boundary",
        "type",
        "start",
        "startInfo",
        "contentDescription"
})
public class MessageHeaderType {

    @XmlElement(name = "Content-Type", required = true)
    protected String contentType;
    @XmlElement(name = "Boundary", required = true)
    protected String boundary;
    @XmlElement(name = "Type", required = true)
    @XmlSchemaType(name = "string")
    protected TypeType type;
    @XmlElement(name = "Start", required = true)
    protected String start;
    @XmlElement(name = "StartInfo")
    protected String startInfo;
    @XmlElement(name = "Content-Description")
    protected String contentDescription;

    /**
     * Gets the value of the contentType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Gets the value of the boundary property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Sets the value of the boundary property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBoundary(String value) {
        this.boundary = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link TypeType }
     *
     */
    public TypeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link TypeType }
     *
     */
    public void setType(TypeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the start property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStart(String value) {
        this.start = value;
    }

    /**
     * Gets the value of the startInfo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStartInfo() {
        return startInfo;
    }

    /**
     * Sets the value of the startInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStartInfo(String value) {
        this.startInfo = value;
    }

    /**
     * Gets the value of the contentDescription property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContentDescription() {
        return contentDescription;
    }

    /**
     * Sets the value of the contentDescription property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContentDescription(String value) {
        this.contentDescription = value;
    }

}
