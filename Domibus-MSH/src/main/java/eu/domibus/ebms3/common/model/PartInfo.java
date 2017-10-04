package eu.domibus.ebms3.common.model;

import eu.domibus.common.AutoCloseFileDataSource;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartInfo", propOrder = {"schema", "description", "partProperties"})
@NamedQueries(@NamedQuery(name = "PartInfo.loadBinaryData", query = "select pi.binaryData from PartInfo pi where pi.entityId=:ENTITY_ID"))
@Entity
@Table(name = "TB_PART_INFO")
public class PartInfo extends AbstractBaseEntity implements Comparable<PartInfo> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartInfo.class);

    @XmlElement(name = "Schema")
    @Embedded
    protected Schema schema; //NOSONAR

    @XmlElement(name = "Description")
    @Embedded
    protected Description description; //NOSONAR

    @XmlElement(name = "PartProperties")
    @Embedded
    protected PartProperties partProperties; //NOSONAR

    @XmlAttribute(name = "href")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    @Column(name = "HREF")
    protected String href;

    @XmlTransient
    @Lob
    @Column(name = "BINARY_DATA")
    @Basic(fetch = FetchType.EAGER)
    protected byte[] binaryData;

    @XmlTransient
    @Column(name = "FILENAME")
    protected String fileName;

    @XmlTransient
    @Column(name = "IN_BODY")
    protected boolean inBody;
    @Transient
    @XmlTransient
    protected DataHandler payloadDatahandler; //NOSONAR
    @Column(name = "MIME")
    @XmlTransient
    private String mime;

    public DataHandler getPayloadDatahandler() {
        return payloadDatahandler;
    }

    public void setPayloadDatahandler(final DataHandler payloadDatahandler) {
        this.payloadDatahandler = payloadDatahandler;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(final String mime) {
        this.mime = mime;
    }

    public boolean isInBody() {
        return this.inBody;
    }

    public void setInBody(final boolean inBody) {
        this.inBody = inBody;
    }

    public String getFileName() {
        return fileName;
    }

    @PostLoad
    private void loadBinaray() {
        if (fileName != null) { /* Create payload data handler from File */
            LOG.debug("LoadBinary from file: " + fileName);
            payloadDatahandler = new DataHandler(new AutoCloseFileDataSource(fileName));
            return;
        }
        /* Create payload data handler from binaryData (byte[]) */
        if(binaryData == null) {
            LOG.info("Payload is empty!");
            payloadDatahandler = null;
        } else {
            payloadDatahandler = new DataHandler(new ByteArrayDataSource(binaryData, mime));
        }

    }

    /**
     * This element occurs zero or more times. It refers to schema(s) that define the instance document
     * identified in the parent PartInfo element. If the item being referenced has schema(s) of some kind
     * that describe it (e.g. an XML Schema, DTD and/or a database schema), then the Schema
     * element SHOULD be present as a child of the PartInfo element. It provides a means of identifying
     * the schema and its version defining the payload object identified by the parent PartInfo element.
     * This metadata MAY be used to validate the Payload Part to which it refers, but the MSH is NOT
     * REQUIRED to do so. The Schema element contains the following attributes:
     * · (a) namespace - the OPTIONAL target namespace of the schema
     * · (b) location – the REQUIRED URI of the schema
     * · (c) version – an OPTIONAL version identifier of the schema.
     *
     * @return possible object is {@link Schema }
     */
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * This element occurs zero or more times. It refers to schema(s) that define the instance document
     * identified in the parent PartInfo element. If the item being referenced has schema(s) of some kind
     * that describe it (e.g. an XML Schema, DTD and/or a database schema), then the Schema
     * element SHOULD be present as a child of the PartInfo element. It provides a means of identifying
     * the schema and its version defining the payload object identified by the parent PartInfo element.
     * This metadata MAY be used to validate the Payload Part to which it refers, but the MSH is NOT
     * REQUIRED to do so. The Schema element contains the following attributes:
     * · (a) namespace - the OPTIONAL target namespace of the schema
     * · (b) location – the REQUIRED URI of the schema
     * · (c) version – an OPTIONAL version identifier of the schema.
     *
     * @param value allowed object is {@link Schema }
     */
    public void setSchema(final Schema value) {
        this.schema = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link Description }
     */
    public Description getDescription() {
        return this.description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link Description }
     */
    public void setDescription(final Description value) {
        this.description = value;
    }

    /**
     * This element has zero or more eb:Property child elements. An eb:Property element is of
     * xs:anySimpleType (e.g. string, URI) and has a REQUIRED @name attribute, the value of which
     * must be agreed between partners. Its actual semantics is beyond the scope of this specification.
     * The element is intended to be consumed outside the ebMS specified functions. It may contain
     * meta-data that qualifies or abstracts the payload data. A representation in the header of such
     * properties allows for more efficient monitoring, correlating, dispatching and validating functions
     * (even if these are out of scope of ebMS specification) that do not require payload access
     *
     * @return possible object is {@link PartProperties }
     */
    public PartProperties getPartProperties() {
        return this.partProperties;
    }

    /**
     * This element has zero or more eb:Property child elements. An eb:Property element is of
     * xs:anySimpleType (e.g. string, URI) and has a REQUIRED @name attribute, the value of which
     * must be agreed between partners. Its actual semantics is beyond the scope of this specification.
     * The element is intended to be consumed outside the ebMS specified functions. It may contain
     * meta-data that qualifies or abstracts the payload data. A representation in the header of such
     * properties allows for more efficient monitoring, correlating, dispatching and validating functions
     * (even if these are out of scope of ebMS specification) that do not require payload access
     *
     * @param value allowed object is {@link PartProperties }
     */
    public void setPartProperties(final PartProperties value) {
        this.partProperties = value;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("schema", schema)
                .append("description", description)
                .append("partProperties", partProperties)
                .append("href", href)
                .append("binaryData", binaryData)
                .append("fileName", fileName)
                .append("inBody", inBody)
                .append("payloadDatahandler", payloadDatahandler)
                .append("mime", mime)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartInfo partInfo = (PartInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(schema, partInfo.schema)
                .append(description, partInfo.description)
                //.append(partProperties, partInfo.partProperties)
                .append(href, partInfo.href)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(schema)
                .append(description)
                // .append(partProperties)
                .append(href)
                .toHashCode();
    }

    @Override
    public int compareTo(final PartInfo o) {
        return this.hashCode() - o.hashCode();
    }
}
