package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.activation.DataHandler;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class PartInfo {

    protected Schema schema;

    protected Description description;

    protected PartProperties partProperties;

    protected String href;

    protected boolean inBody;

    protected String mime;

    protected DataHandler payloadDatahandler;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public PartProperties getPartProperties() {
        return partProperties;
    }

    public void setPartProperties(PartProperties partProperties) {
        this.partProperties = partProperties;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public boolean getInBody() {
        return inBody;
    }

    public void setInBody(boolean inBody) {
        this.inBody = inBody;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public DataHandler getPayloadDatahandler() {
        return payloadDatahandler;
    }

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
