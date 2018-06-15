package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class for Part Info details
 *
 * It stores information about Part Info
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class PartInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Schema information  {@link SchemaDTO}
     */
    private SchemaDTO schema;

    /**
     * Description information {@link DescriptionDTO}
     */
    private DescriptionDTO description;

    /**
     * Part Properties details {@link PartPropertiesDTO}
     */
    private PartPropertiesDTO partProperties;

    /**
     * HRef link {@link String}
     */
    private String href;

    /**
     * InBody information
     */
    private boolean inBody;

    /**
     * MIME {@link String}
     */
    private String mime;

    /**
     * Gets the Schema information
     * @return Schema information {@link SchemaDTO}
     */
    public SchemaDTO getSchema() {
        return schema;
    }

    /**
     * Sets the Schema information
     * @param schema Schema information {@link SchemaDTO}
     */
    public void setSchema(SchemaDTO schema) {
        this.schema = schema;
    }

    /**
     * Gets Description information
     * @return Description information {@link DescriptionDTO}
     */
    public DescriptionDTO getDescription() {
        return description;
    }

    /**
     * Sets Description information
     * @param description Description information {@link DescriptionDTO}
     */
    public void setDescription(DescriptionDTO description) {
        this.description = description;
    }

    /**
     * Gets Part Properties details
     * @return Part Properties details {@link PartPropertiesDTO}
     */
    public PartPropertiesDTO getPartProperties() {
        return partProperties;
    }

    /**
     * Sets Part Properties details
     * @param partProperties Part Properties details {@link PartPropertiesDTO}
     */
    public void setPartProperties(PartPropertiesDTO partProperties) {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("schema", schema)
                .append("description", description)
                .append("partProperties", partProperties)
                .append("href", href)
                .append("inBody", inBody)
                .append("mime", mime)
                .toString();
    }
}
