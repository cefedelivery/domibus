package eu.domibus.plugin.webService.impl;


import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlTransient;


/**
 * <p>
 * This element occurs zero or more times. The ExtendedPartInfo element is used to reference a MIME
 * attachment, an XML element within the SOAP Body, or another resource which may be obtained
 * by resolving a URL, according to the value of the href attribute property.
 * Any other namespace-qualified attribute MAY be present. A Receiving MSH MAY choose to ignore any
 * foreign namespace attributes other than those defined above.
 * The designer of the business process or information exchange using ebXML Messaging decides what
 * payload data is referenced by the Manifest and the values to be used for xlink:role.
 *
 * @author Christian Koch, Martini Federico
 * @version 2.0
 * @since 3.0
 */
public class ExtendedPartInfo extends PartInfo {

    @XmlTransient
    protected byte[] binaryData;

    @XmlTransient
    protected boolean inBody;

    @XmlTransient
    protected transient DataHandler payloadDatahandler;

    public ExtendedPartInfo() {

    }

    public ExtendedPartInfo(PartInfo partInfo) {
        this.setHref(partInfo.getHref());
        this.setPartProperties(partInfo.getPartProperties());
    }

    /**
     * This OPTIONAL attribute has a value that is the [RFC2392] Content-ID URI of the payload object
     * referenced, an xml:id fragment identifier, or the URL of an externally referenced resource; for
     * example, "cid:foo@example.com" or "#idref". The absence of the attribute href in the element
     * eb:ExtendedPartInfo indicates that the payload part being referenced is the SOAP Body element itself. For
     * example, a declaration of the following form simply indicates that the entire SOAP Body is to be
     * considered a payload part in this ebMS message:
     * {@code
     * <eb:PayloadInfo>
     * <eb:ExtendedPartInfo/>
     * </eb:PayloadInfo>}
     *
     * @return possible object is {@link String }
     */
    public String getHref() {
        return this.href;
    }

    /**
     * This OPTIONAL attribute has a value that is the [RFC2392] Content-ID URI of the payload object
     * referenced, an xml:id fragment identifier, or the URL of an externally referenced resource; for
     * example, "cid:foo@example.com" or "#idref". The absence of the attribute href in the element
     * eb:ExtendedPartInfo indicates that the payload part being referenced is the SOAP Body element itself. For
     * example, a declaration of the following form simply indicates that the entire SOAP Body is to be
     * considered a payload part in this ebMS message:
     * {@code
     * <eb:PayloadInfo>
     * <eb:ExtendedPartInfo/>
     * </eb:PayloadInfo>}
     *
     * @param value allowed object is {@link String }
     */
    public void setHref(final String value) {
        this.href = value;
    }


    public DataHandler getPayloadDatahandler() {
        return payloadDatahandler;
    }

    public void setPayloadDatahandler(final DataHandler payloadDatahandler) {
        this.payloadDatahandler = payloadDatahandler;
    }

    public boolean isInBody() {
        return this.inBody;
    }

    public void setInBody(final boolean inBody) {
        this.inBody = inBody;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("HRef", href);
        builder.append("partProperties", partProperties.getProperty());
        builder.append("InBody", inBody);
        builder.append("PayloadDatahandler", payloadDatahandler);
        builder.append("BinaryData", binaryData);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ExtendedPartInfo that = (ExtendedPartInfo) o;

        return new EqualsBuilder()
                .append(super.getHref(), that.getHref())
                .append(super.getPartProperties(), that.getPartProperties())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getHref())
                .append(getPartProperties())
                .toHashCode();
    }
}
