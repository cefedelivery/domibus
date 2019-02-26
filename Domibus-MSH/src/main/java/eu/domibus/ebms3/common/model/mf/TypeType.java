package eu.domibus.ebms3.common.model.mf;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * Auto-generated JAXB class based on the SplitAndJoin XSD
 *
 * <p>Java class for TypeType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TypeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="application/xop+xml"/&gt;
 *     &lt;enumeration value="text/xml"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@XmlType(name = "TypeType", namespace = "http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/")
@XmlEnum
public enum TypeType {


    /**
     * XOP Package as defined in
     *                         http://www.w3.org/TR/2005/REC-xop10-20050125/ 
     *
     */
    @XmlEnumValue("application/xop+xml")
    APPLICATION_XOP_XML("application/xop+xml"),

    /**
     * SOAP with attachments 
     *
     */
    @XmlEnumValue("text/xml")
    TEXT_XML("text/xml");
    private final String value;

    TypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypeType fromValue(String v) {
        for (TypeType c : TypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
