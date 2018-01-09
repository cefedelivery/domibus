package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class Description {

    /**
     * Description details {@link String}
     */
    protected String value;

    /**
     * Description language {@link String}
     */
    protected String lang;

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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Description that = (Description) o;

        return new EqualsBuilder()
                .append(value, that.value)
                .append(lang, that.lang)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(lang)
                .toHashCode();
    }
}
