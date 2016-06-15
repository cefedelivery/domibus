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

public class Description {

    @XmlValue

    protected String value;
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")

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
        return this.value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = this.value.hashCode();
        result = 31 * result + (this.lang != null ? this.lang.hashCode() : 0);
        return result;
    }
}
