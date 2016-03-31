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
 * This type defines the structure for PartyInfo Definition. The "type"
 * attribute identifies the type of PartyId which is the content for PartyInfo type
 * being defined here.
 * <p/>
 * <p>Java class for PartyInfoType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="PartyInfoType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0>non-empty-string">
 *       &lt;attribute name="type" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartyInfoType", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", propOrder = {
        "value"
})
public class PartyInfoType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "type")
    protected String type;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setType(final String value) {
        this.type = value;
    }

}
