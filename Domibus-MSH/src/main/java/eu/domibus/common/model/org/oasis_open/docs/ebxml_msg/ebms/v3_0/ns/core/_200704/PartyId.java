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

import eu.domibus.common.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.*;

/**
 * The
 * REQUIRED PartyId element occurs one or more times. If it occurs multiple times, each instance
 * MUST identify the same organization.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartyId", propOrder = "value")
@Entity
@Table(name = "TB_PARTY_ID")
public class PartyId extends AbstractBaseEntity implements Comparable<PartyId> {

    @XmlValue
    @Column(name = "VALUE")
    protected String value;
    @XmlAttribute(name = "type")
    @Column(name = "TYPE")
    protected String type;

    /**
     * gets the party identifier.
     *
     * @return string value content that identifies a party, or that is one of the identifiers of this party.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the party identifier.
     *
     * @param value string value content that identifies a party, or that is one of the identifiers of this party.
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * The type attribute indicates the domain of names to which the string in the
     * content of the PartyId element belongs. It is RECOMMENDED that the value of the type attribute be a
     * URI. It is further RECOMMENDED that these values be taken from the EDIRA , EDIFACT or ANSI ASC
     * X12 registries. Technical specifications for the first two registries can be found at and [ISO6523] and
     * [ISO9735], respectively. Further discussion of PartyId types and methods of construction can be found in
     * an appendix of [ebCPPA21]. The value of any given @type attribute MUST be unique within a list of
     * PartyId elements.
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return this.type;
    }

    /**
     * The type attribute indicates the domain of names to which the string in the
     * content of the PartyId element belongs. It is RECOMMENDED that the value of the type attribute be a
     * URI. It is further RECOMMENDED that these values be taken from the EDIRA , EDIFACT or ANSI ASC
     * X12 registries. Technical specifications for the first two registries can be found at and [ISO6523] and
     * [ISO9735], respectively. Further discussion of PartyId types and methods of construction can be found in
     * an appendix of [ebCPPA21]. The value of any given @type attribute MUST be unique within a list of
     * PartyId elements.
     *
     * @param value allowed object is {@link String }
     */
    public void setType(final String value) {
        this.type = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PartyId)) return false;
        if (!super.equals(o)) return false;

        final PartyId partyId = (PartyId) o;

        if (this.type != null ? !this.type.equals(partyId.type) : partyId.type != null) return false;
        return this.value.equals(partyId.value);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.value.hashCode();
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final PartyId o) {
        return this.hashCode() - o.hashCode();
    }
}
