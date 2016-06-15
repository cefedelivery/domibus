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

package eu.domibus.common.model.org.xmlsoap.schemas.soap.envelope;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;

/**
 * Fault reporting structure
 * <p/>
 * <p/>
 * <p/>
 * Java class for Fault complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="Fault">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="faultcode" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="faultstring" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="faultactor" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="detail" type="{http://schemas.xmlsoap.org/soap/envelope/}detail" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Fault", propOrder = {"faultcode", "faultstring", "faultactor", "detail"})
public class Fault {

    @XmlElement(required = true)
    protected QName faultcode;
    @XmlElement(required = true)
    protected String faultstring;
    @XmlSchemaType(name = "anyURI")
    protected String faultactor;
    protected Detail detail;

    /**
     * Gets the value of the faultcode property.
     *
     * @return possible object is {@link QName }
     */
    public QName getFaultcode() {
        return this.faultcode;
    }

    /**
     * Sets the value of the faultcode property.
     *
     * @param value allowed object is {@link QName }
     */
    public void setFaultcode(final QName value) {
        this.faultcode = value;
    }

    /**
     * Gets the value of the faultstring property.
     *
     * @return possible object is {@link String }
     */
    public String getFaultstring() {
        return this.faultstring;
    }

    /**
     * Sets the value of the faultstring property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFaultstring(final String value) {
        this.faultstring = value;
    }

    /**
     * Gets the value of the faultactor property.
     *
     * @return possible object is {@link String }
     */
    public String getFaultactor() {
        return this.faultactor;
    }

    /**
     * Sets the value of the faultactor property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFaultactor(final String value) {
        this.faultactor = value;
    }

    /**
     * Gets the value of the detail property.
     *
     * @return possible object is {@link Detail }
     */
    public Detail getDetail() {
        return this.detail;
    }

    /**
     * Sets the value of the detail property.
     *
     * @param value allowed object is {@link Detail }
     */
    public void setDetail(final Detail value) {
        this.detail = value;
    }
}
