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

package eu.domibus.plugin.webService.generated;

import javax.xml.bind.annotation.*;

/**
 * <p/>
 * Java class for anonymous complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="code">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="ERROR_GENERAL_001"/>
 *               &lt;enumeration value="ERROR_GENERAL_002"/>
 *               &lt;enumeration value="ERROR_GENERAL_003"/>
 *               &lt;enumeration value="ERROR_SEND_001"/>
 *               &lt;enumeration value="ERROR_SEND_002"/>
 *               &lt;enumeration value="ERROR_SEND_003"/>
 *               &lt;enumeration value="ERROR_SEND_004"/>
 *               &lt;enumeration value="ERROR_SEND_005"/>
 *               &lt;enumeration value="ERROR_DOWNLOAD_001"/>
 *               &lt;enumeration value="ERROR_DOWNLOAD_002"/>
 *               &lt;enumeration value="ERROR_DOWNLOAD_003"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"code", "message"})
@XmlRootElement(name = "FaultDetail")
public class FaultDetail {

    @XmlElement(required = true)
    protected String code;
    @XmlElement(required = true, nillable = true)
    protected String message;

    /**
     * Gets the value of the code property.
     *
     * @return possible object is {@link String }
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCode(final String value) {
        this.code = value;
    }

    /**
     * Gets the value of the message property.
     *
     * @return possible object is {@link String }
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets the value of the message property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMessage(final String value) {
        this.message = value;
    }
}
