package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.activation.DataHandler;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class PartInfo {

    /**
     * Schema information  {@link Schema}
     */
    protected Schema schema;

    /**
     * Description information {@link Description}
     */
    protected Description description;

    /**
     * Part Properties details {@link PartProperties}
     */
    protected PartProperties partProperties;

    /**
     * HRef link {@link String}
     */
    protected String href;

    /**
     * InBody information
     */
    protected boolean inBody;

    /**
     * MIME {@link String}
     */
    protected String mime;

    /**
     * Payload Data Handler detailed info {@link DataHandler}
     */
    protected DataHandler payloadDatahandler;

    /**
     * Gets the Schema information
     * @return Schema information {@link Schema}
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Sets the Schema information
     * @param schema Schema information {@link Schema}
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * Gets Description information
     * @return Description information {@link Description}
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets Description information
     * @param description Description information {@link Description}
     */
    public void setDescription(Description description) {
        this.description = description;
    }

    /**
     * Gets Part Properties details
     * @return Part Properties details {@link PartProperties}
     */
    public PartProperties getPartProperties() {
        return partProperties;
    }

    /**
     * Sets Part Properties details
     * @param partProperties Part Properties details {@link PartProperties}
     */
    public void setPartProperties(PartProperties partProperties) {
        this.partProperties = partProperties;
    }

    /**
     * Gets HRef link
     * @return HRef link {@link String}
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets HRef link
     * @param href HRef link {@link String}
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * Gets InBody information
     * @return InBody information
     */
    public boolean getInBody() {
        return inBody;
    }

    /**
     * Sets InBody information
     * @param inBody InBody information
     */
    public void setInBody(boolean inBody) {
        this.inBody = inBody;
    }

    /**
     * Gets MIME
     * @return MIME {@link String}
     */
    public String getMime() {
        return mime;
    }

    /**
     * Sets MIME
     * @param mime MIME {@link String}
     */
    public void setMime(String mime) {
        this.mime = mime;
    }

    /**
     * Gets Payload Data Handler detailed info
     * @return Payload Data Handler detailed info {@link DataHandler}
     */
    public DataHandler getPayloadDatahandler() {
        return payloadDatahandler;
    }

    /**
     * Sets Payload Data Handler detailed info
     * @param payloadDatahandler Payload Data Handler detailed info {@link DataHandler}
     */
    public void setPayloadDatahandler(DataHandler payloadDatahandler) {
        this.payloadDatahandler = payloadDatahandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartInfo partInfo = (PartInfo) o;

        return new EqualsBuilder()
                .append(inBody, partInfo.inBody)
                .append(schema, partInfo.schema)
                .append(description, partInfo.description)
                .append(partProperties, partInfo.partProperties)
                .append(href, partInfo.href)
                .append(mime, partInfo.mime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(schema)
                .append(description)
                .append(partProperties)
                .append(href)
                .append(inBody)
                .append(mime)
                .toHashCode();
    }
}
