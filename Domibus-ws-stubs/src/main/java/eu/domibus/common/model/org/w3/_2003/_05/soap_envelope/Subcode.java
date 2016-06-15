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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * <p/>
 * Java class for subcode complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="subcode">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="Subcode" type="{http://www.w3.org/2003/05/soap-envelope}subcode" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@SuppressWarnings("ALL")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subcode", propOrder = {"value", "subcode"})
public class Subcode {

    @XmlElement(name = "Value", required = true)
    protected QName value;
    @XmlElement(name = "Subcode")
    protected Subcode subcode;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link QName }
     */
    public QName getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link QName }
     */
    public void setValue(final QName value) {
        this.value = value;
    }

    /**
     * Gets the value of the subcode property.
     *
     * @return possible object is {@link Subcode }
     */
    public Subcode getSubcode() {
        return subcode;
    }

    /**
     * Sets the value of the subcode property.
     *
     * @param value allowed object is {@link Subcode }
     */
    public void setSubcode(final Subcode value) {
        this.subcode = value;
    }
}
