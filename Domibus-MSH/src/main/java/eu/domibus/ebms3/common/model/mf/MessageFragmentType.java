package eu.domibus.ebms3.common.model.mf;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Auto-generated JAXB class based on the SplitAndJoin XSD
 *
 * <p>Java class for MessageFragmentType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="MessageFragmentType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="GroupId" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}non-empty-string"/&gt;
 *         &lt;element name="MessageSize" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/&gt;
 *         &lt;element name="FragmentCount" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/&gt;
 *         &lt;element name="FragmentNum" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/&gt;
 *         &lt;element name="MessageHeader" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}MessageHeaderType" minOccurs="0"/&gt;
 *         &lt;element name="Action" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;sequence minOccurs="0"&gt;
 *           &lt;element name="CompressionAlgorithm" type="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}CompressionAlgorithmType"/&gt;
 *           &lt;element name="CompressedMessageSize" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/&gt;
 *         &lt;/sequence&gt;
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/}S12atts"/&gt;
 *       &lt;attribute name="href" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
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
@XmlType(name = "MessageFragmentType", namespace = "http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/", propOrder = {
        "groupId",
        "messageSize",
        "fragmentCount",
        "fragmentNum",
        "messageHeader",
        "action",
        "compressionAlgorithm",
        "compressedMessageSize",
        "any"
})
@XmlRootElement(name = "MessageFragment")
public class MessageFragmentType {

    @XmlElement(name = "GroupId", required = true)
    protected String groupId;
    @XmlElement(name = "MessageSize")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger messageSize;
    @XmlElement(name = "FragmentCount")
    @XmlSchemaType(name = "positiveInteger")
    protected Long fragmentCount;
    @XmlElement(name = "FragmentNum", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected Long fragmentNum;
    @XmlElement(name = "MessageHeader")
    protected MessageHeaderType messageHeader;
    @XmlElement(name = "Action")
    protected String action;
    @XmlElement(name = "CompressionAlgorithm")
    @XmlSchemaType(name = "anySimpleType")
    protected String compressionAlgorithm;
    @XmlElement(name = "CompressedMessageSize")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger compressedMessageSize;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(name = "href")
    @XmlSchemaType(name = "anyURI")
    protected String href;
    @XmlAttribute(name = "mustUnderstand", namespace = "http://www.w3.org/2003/05/soap-envelope")
    protected Boolean mustUnderstand;
    @XmlAttribute(name = "encodingStyle", namespace = "http://www.w3.org/2003/05/soap-envelope")
    @XmlSchemaType(name = "anyURI")
    protected String encodingStyle;
    @XmlAttribute(name = "relay", namespace = "http://www.w3.org/2003/05/soap-envelope")
    protected Boolean relay;
    @XmlAttribute(name = "role", namespace = "http://www.w3.org/2003/05/soap-envelope")
    @XmlSchemaType(name = "anyURI")
    protected String role;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the groupId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the messageSize property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public BigInteger getMessageSize() {
        return messageSize;
    }

    /**
     * Sets the value of the messageSize property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setMessageSize(BigInteger value) {
        this.messageSize = value;
    }

    /**
     * Gets the value of the fragmentCount property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getFragmentCount() {
        return fragmentCount;
    }

    /**
     * Sets the value of the fragmentCount property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setFragmentCount(Long value) {
        this.fragmentCount = value;
    }

    /**
     * Gets the value of the fragmentNum property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getFragmentNum() {
        return fragmentNum;
    }

    /**
     * Sets the value of the fragmentNum property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setFragmentNum(Long value) {
        this.fragmentNum = value;
    }

    /**
     * Gets the value of the messageHeader property.
     *
     * @return
     *     possible object is
     *     {@link MessageHeaderType }
     *
     */
    public MessageHeaderType getMessageHeader() {
        return messageHeader;
    }

    /**
     * Sets the value of the messageHeader property.
     *
     * @param value
     *     allowed object is
     *     {@link MessageHeaderType }
     *
     */
    public void setMessageHeader(MessageHeaderType value) {
        this.messageHeader = value;
    }

    /**
     * Gets the value of the action property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the compressionAlgorithm property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    /**
     * Sets the value of the compressionAlgorithm property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCompressionAlgorithm(String value) {
        this.compressionAlgorithm = value;
    }

    /**
     * Gets the value of the compressedMessageSize property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public BigInteger getCompressedMessageSize() {
        return compressedMessageSize;
    }

    /**
     * Sets the value of the compressedMessageSize property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setCompressedMessageSize(BigInteger value) {
        this.compressedMessageSize = value;
    }

    /**
     * Gets the value of the any property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Element }
     *
     *
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the href property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     *  if SOAP 1.2 is being used, this attribute is required, other
     *                     attributes in the S12atts group are allowed and attributes in the S11atts group
     *                     are prohibited.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isMustUnderstand() {
        if (mustUnderstand == null) {
            return false;
        } else {
            return mustUnderstand;
        }
    }

    /**
     * Sets the value of the mustUnderstand property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMustUnderstand(Boolean value) {
        this.mustUnderstand = value;
    }

    /**
     * Gets the value of the encodingStyle property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEncodingStyle() {
        return encodingStyle;
    }

    /**
     * Sets the value of the encodingStyle property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEncodingStyle(String value) {
        this.encodingStyle = value;
    }

    /**
     * Gets the value of the relay property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isRelay() {
        if (relay == null) {
            return false;
        } else {
            return relay;
        }
    }

    /**
     * Sets the value of the relay property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRelay(Boolean value) {
        this.relay = value;
    }

    /**
     * Gets the value of the role property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRole(String value) {
        this.role = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     *
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     *
     *
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }
}
