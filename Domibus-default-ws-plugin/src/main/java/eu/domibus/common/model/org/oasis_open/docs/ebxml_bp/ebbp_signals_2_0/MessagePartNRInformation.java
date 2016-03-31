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

package eu.domibus.common.model.org.oasis_open.docs.ebxml_bp.ebbp_signals_2_0;

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
 *       &lt;choice>
 *         &lt;element name="MessagePartIdentifier" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Reference"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "messagePartIdentifier",
        "reference"
})
@XmlRootElement(name = "MessagePartNRInformation", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
public class MessagePartNRInformation {

    @XmlElement(name = "MessagePartIdentifier", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected String messagePartIdentifier;
    @XmlElement(name = "Reference", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected ReferenceType reference;

    /**
     * Gets the value of the messagePartIdentifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMessagePartIdentifier() {
        return this.messagePartIdentifier;
    }

    /**
     * Sets the value of the messagePartIdentifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMessagePartIdentifier(final String value) {
        this.messagePartIdentifier = value;
    }

    /**
     * Gets the value of the reference property.
     *
     * @return possible object is
     * {@link ReferenceType }
     */
    public ReferenceType getReference() {
        return this.reference;
    }

    /**
     * Sets the value of the reference property.
     *
     * @param value allowed object is
     *              {@link ReferenceType }
     */
    public void setReference(final ReferenceType value) {
        this.reference = value;
    }

}
