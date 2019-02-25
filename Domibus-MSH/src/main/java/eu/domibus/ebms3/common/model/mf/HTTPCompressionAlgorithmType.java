package eu.domibus.ebms3.common.model.mf;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * Auto-generated JAXB class based on the SplitAndJoin XSD
 *
 * <p>Java class for HTTPCompressionAlgorithmType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HTTPCompressionAlgorithmType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="gzip"/&gt;
 *     &lt;enumeration value="compress"/&gt;
 *     &lt;enumeration value="deflate"/&gt;
 *     &lt;enumeration value="identity"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@XmlType(name = "HTTPCompressionAlgorithmType", namespace = "http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/")
@XmlEnum
public enum HTTPCompressionAlgorithmType {

    @XmlEnumValue("gzip")
    GZIP("gzip"),
    @XmlEnumValue("compress")
    COMPRESS("compress"),
    @XmlEnumValue("deflate")
    DEFLATE("deflate"),
    @XmlEnumValue("identity")
    IDENTITY("identity");
    private final String value;

    HTTPCompressionAlgorithmType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HTTPCompressionAlgorithmType fromValue(String v) {
        for (HTTPCompressionAlgorithmType c : HTTPCompressionAlgorithmType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
