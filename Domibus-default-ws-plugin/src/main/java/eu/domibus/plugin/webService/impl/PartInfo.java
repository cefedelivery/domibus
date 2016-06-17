package eu.domibus.plugin.webService.impl;


import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlTransient;


/**
 * <p/>
 * This element occurs zero or more times. The PartInfo element is used to reference a MIME
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
public class PartInfo extends eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo {

    @XmlTransient
    protected byte[] binaryData;

    @XmlTransient
    protected boolean inBody;
    @XmlTransient
    protected DataHandler payloadDatahandler;

    void PartInfo(eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo realPartInfo) {
        this.setHref(realPartInfo.getHref());
        this.setSchema(realPartInfo.getSchema());
        this.setDescription(realPartInfo.getDescription());
        this.setPartProperties(realPartInfo.getPartProperties());
    }

    /**
     * This OPTIONAL attribute has a value that is the [RFC2392] Content-ID URI of the payload object
     * referenced, an xml:id fragment identifier, or the URL of an externally referenced resource; for
     * example, "cid:foo@example.com" or "#idref". The absence of the attribute href in the element
     * eb:PartInfo indicates that the payload part being referenced is the SOAP Body element itself. For
     * example, a declaration of the following form simply indicates that the entire SOAP Body is to be
     * considered a payload part in this ebMS message:
     * {@code
     * <eb:PayloadInfo>
     * <eb:PartInfo/>
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
     * eb:PartInfo indicates that the payload part being referenced is the SOAP Body element itself. For
     * example, a declaration of the following form simply indicates that the entire SOAP Body is to be
     * considered a payload part in this ebMS message:
     * {@code
     * <eb:PayloadInfo>
     * <eb:PartInfo/>
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
        return "PartInfo{" +
                "schema=" + this.schema +
                ", description=" + this.description +
                ", partProperties=" + this.partProperties +
                ", href='" + this.href + '\'' +

                '}';
    }

    @Override
    public boolean equals(final Object o) { //FIXME: can we do equals without considering the binary data?
        if (this == o) return true;
        if (!(o instanceof PartInfo)) return false;

        final PartInfo partInfo = (PartInfo) o;

        if (this.description != null ? !this.description.equals(partInfo.description) : partInfo.description != null)
            return false;
        if (this.href != null ? !this.href.equals(partInfo.href) : partInfo.href != null) return false;
        if (this.partProperties != null ? !this.partProperties.equals(partInfo.partProperties) : partInfo.partProperties != null)
            return false;
        return !(this.schema != null ? !this.schema.equals(partInfo.schema) : partInfo.schema != null);

    }

    @Override
    public int hashCode() {
        int result = 31;
        result = 31 * result + (this.schema != null ? this.schema.hashCode() : 0);
        result = 31 * result + (this.description != null ? this.description.hashCode() : 0);
        result = 31 * result + (this.partProperties != null ? this.partProperties.hashCode() : 0);
        result = 31 * result + (this.href != null ? this.href.hashCode() : 0);
        return result;
    }
}
