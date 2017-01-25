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

package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="partyId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="partyIdType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_PARTY_IDENTIFIER")
@NamedQuery(name = "Identifier.findByTypeAndPartyId", query = "select i from Identifier i where i.partyId = :PARTY_ID and ((:PARTY_ID_TYPE is null and i.partyIdType.value is null) or i.partyIdType.value  = :PARTY_ID_TYPE)")
public class Identifier extends AbstractBaseEntity {

    @XmlAttribute(name = "partyId", required = true)
    @Column(name = "PARTY_ID")
    protected String partyId;
    @XmlAttribute(name = "partyIdType", required = true)
    @Transient
    protected String partyIdTypeXml;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_PARTY_ID_TYPE")
    private PartyIdType partyIdType;

    /**
     * Gets the value of the partyId property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartyId() {
        return this.partyId;
    }

    /**
     * Sets the value of the partyId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartyId(final String value) {
        this.partyId = value;
    }

    /**
     * Gets the value of the partyIdType property.
     *
     * @return possible object is
     * {@link String }
     */
    public PartyIdType getPartyIdType() {
        return this.partyIdType;
    }

    /**
     * Sets the value of the partyIdType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartyIdType(final PartyIdType value) {
        this.partyIdType = value;
    }

    public void init(final Configuration configuration) {
        for (final PartyIdType idType : configuration.getBusinessProcesses().getPartyIdTypes()) {
            if (idType.getName().equals(this.partyIdTypeXml)) {
                this.partyIdType = idType;
                break;
            }
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(partyId, that.partyId)
                .append(partyIdType, that.partyIdType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(partyId)
                .append(partyIdType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("partyId", partyId)
                .append("partyIdType", partyIdType)
                .toString();
    }

}
