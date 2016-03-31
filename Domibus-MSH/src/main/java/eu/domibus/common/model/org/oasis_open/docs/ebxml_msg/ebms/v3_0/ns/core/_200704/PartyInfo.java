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

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This REQUIRED element occurs once,
 * and contains data about originating party and destination party.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartyInfo", propOrder = {"from", "to"})
@Embeddable
public class PartyInfo {

    @XmlElement(name = "From", required = true)
    @Embedded
    protected From from;
    @XmlElement(name = "To", required = true)
    @Embedded
    protected To to;

    /**
     * The REQUIRED element
     * occurs once, and contains information describing the originating party.
     *
     * @return possible object is {@link From }
     */
    public From getFrom() {
        return this.from;
    }

    /**
     * The REQUIRED element
     * occurs once, and contains information describing the originating party.
     *
     * @param value allowed object is {@link From }
     */
    public void setFrom(final From value) {
        this.from = value;
    }

    /**
     * The REQUIRED element occurs
     * once, and contains information describing the destination party.
     *
     * @return possible object is {@link To }
     */
    public To getTo() {
        return this.to;
    }

    /**
     * The REQUIRED element occurs
     * once, and contains information describing the destination party.
     *
     * @param value allowed object is {@link To }
     */
    public void setTo(final To value) {
        this.to = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PartyInfo)) return false;

        final PartyInfo partyInfo = (PartyInfo) o;

        if (this.from != null ? !this.from.equals(partyInfo.from) : partyInfo.from != null) return false;
        return !(this.to != null ? !this.to.equals(partyInfo.to) : partyInfo.to != null);

    }

    @Override
    public int hashCode() {
        int result = this.from != null ? this.from.hashCode() : 0;
        result = 31 * result + (this.to != null ? this.to.hashCode() : 0);
        return result;
    }
}
