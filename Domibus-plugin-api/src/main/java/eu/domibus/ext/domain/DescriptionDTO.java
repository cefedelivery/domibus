package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class for Description information
 *
 * It stores information about Description details
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class DescriptionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Description details {@link String}
     */
    private String value;

    /**
     * Description language {@link String}
     */
    private String lang;

    /**
     * Gets the description value
     * @return Description value {@link String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the description value
     * @param value Description value {@link String}
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the description language
     * @return Description language {@link String}
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the description language
     * @param lang Description language {@link String}
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("lang", lang)
                .toString();
    }
}
