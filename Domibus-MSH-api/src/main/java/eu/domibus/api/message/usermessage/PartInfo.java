package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class PartInfo {

    private Schema schema;

    private Description description;

    private PartProperties partProperties;

    private String href;

    private byte[] binaryData;

    private boolean inBody;

    private String mime;

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

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public boolean isInBody() {
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
                .append(binaryData, partInfo.binaryData)
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
                .append(binaryData)
                .append(inBody)
                .append(mime)
                .toHashCode();
    }
}
