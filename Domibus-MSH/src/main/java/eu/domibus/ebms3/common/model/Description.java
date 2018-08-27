package eu.domibus.ebms3.common.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This OPTIONAL element provides a narrative description of the error/payload in the language defined by the
 * xml:lang attribute. The content of this element is left to implementation-specific decisions.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Description", propOrder = "value")
@Embeddable
public class Description {

    @XmlValue
    @Column(name = "DESCRIPTION_VALUE")
    protected String value;
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    @Column(name = "DESCRIPTION_LANG")
    protected String lang;

    /**
     * gets the narrative description
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the narrative description
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the language of the description.
     *
     * @return possible object is {@link String }
     */
    public String getLang() {
        return this.lang;
    }

    /**
     * Sets the language of the description.
     *
     * @param value allowed object is {@link String }
     */
    public void setLang(final String value) {
        this.lang = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Description)) return false;

        final Description that = (Description) o;

        if (this.lang != null ? !this.lang.equals(that.lang) : that.lang != null) return false;
        return this.value.equalsIgnoreCase(that.value);

    }

    @Override
    public int hashCode() {
        int result = this.value.hashCode();
        result = 31 * result + (this.lang != null ? this.lang.hashCode() : 0);
        return result;
    }
}
