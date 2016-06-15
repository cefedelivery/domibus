/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704;


import javax.activation.DataHandler;
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

public class PartInfo {

    @XmlElement(name = "Schema")

    protected Schema schema;
    @XmlElement(name = "Description")

    protected Description description;
    @XmlElement(name = "PartProperties")

    protected PartProperties partProperties;
    @XmlAttribute(name = "href")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String href;

    @XmlTransient
    protected byte[] binaryData;

    @XmlTransient
    protected boolean inBody;
    @XmlTransient
    protected DataHandler payloadDatahandler;

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
