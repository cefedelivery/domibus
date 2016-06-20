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

package eu.domibus.common.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.*;
import java.util.regex.Pattern;

/**
 * An eb:Property element is of xs:anySimpleType (e.g. string, URI) and has two attributes:
 * • @name: The value of this REQUIRED attribute must be agreed upon between partners.
 * • @type: This OPTIONAL attribute allows for resolution of conflicts between properties with the
 * same name, and may also help with Property grouping, e.g. various elements of an address.
 * Its actual semantics is beyond the scope of this specification. The element is intended to be consumed
 * outside the ebMS-specified functions. It may contain some information that qualifies or abstracts message
 * data, or that allows for binding the message to some business process. A representation in the header of
 * such properties allows for more efficient monitoring, correlating, dispatching and validating functions (even
 * if these are out of scope of ebMS specification) that do not require payload access.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Property", propOrder = "value")

@Entity
@Table(name = "TB_PROPERTY")
public class Property extends AbstractBaseEntity implements Comparable<Property> {

    public static final String MIME_TYPE = "MimeType";
    public static final String CHARSET = "CharacterSet";
    public static final Pattern CHARSET_PATTERN = Pattern.compile("[A-Za-z]([A-Za-z0-9._-])*");

    @XmlValue
    @Column(name = "VALUE")
    protected String value;
    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME", nullable = false)
    protected String name;

    @XmlAttribute(name = "type", required = false)
    @Column(name = "TYPE", nullable = true)
    protected String type;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    @Override
    public String toString() {
        return "Property{" +
                "value='" + value + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;
        Property property = (Property) o;
        if (!value.equals(property.value)) return false;
        if (!name.equals(property.name)) return false;
        return !(type != null ? !type.equals(property.type) : property.type != null);

    }

    @Override
    public int hashCode() {
        int result = 31 * value.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final Property o) {
        return this.hashCode() - o.hashCode();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
