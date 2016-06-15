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

package eu.domibus.common.model.org.w3._2003._05.soap_envelope;

import javax.xml.bind.annotation.*;

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
 *         &lt;element name="Code" type="{http://www.w3.org/2003/05/soap-envelope}faultcode"/>
 *         &lt;element name="Reason" type="{http://www.w3.org/2003/05/soap-envelope}faultreason"/>
 *         &lt;element name="Node" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="Role" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="Detail" type="{http://www.w3.org/2003/05/soap-envelope}detail" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Fault", propOrder = {"code", "reason", "node", "role", "detail"})
public class Fault {

    @XmlElement(name = "Code", required = true)
    protected Faultcode code;
    @XmlElement(name = "Reason", required = true)
    protected Faultreason reason;
    @XmlElement(name = "Node")
    @XmlSchemaType(name = "anyURI")
    protected String node;
    @XmlElement(name = "Role")
    @XmlSchemaType(name = "anyURI")
    protected String role;
    @XmlElement(name = "Detail")
    protected Detail detail;

    /**
     * Gets the value of the code property.
     *
     * @return possible object is {@link Faultcode }
     */
    public Faultcode getCode() {
        return this.code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is {@link Faultcode }
     */
    public void setCode(final Faultcode value) {
        this.code = value;
    }

    /**
     * Gets the value of the reason property.
     *
     * @return possible object is {@link Faultreason }
     */
    public Faultreason getReason() {
        return this.reason;
    }

    /**
     * Sets the value of the reason property.
     *
     * @param value allowed object is {@link Faultreason }
     */
    public void setReason(final Faultreason value) {
        this.reason = value;
    }

    /**
     * Gets the value of the node property.
     *
     * @return possible object is {@link String }
     */
    public String getNode() {
        return this.node;
    }

    /**
     * Sets the value of the node property.
     *
     * @param value allowed object is {@link String }
     */
    public void setNode(final String value) {
        this.node = value;
    }

    /**
     * Gets the value of the role property.
     *
     * @return possible object is {@link String }
     */
    public String getRole() {
        return this.role;
    }

    /**
     * Sets the value of the role property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRole(final String value) {
        this.role = value;
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
