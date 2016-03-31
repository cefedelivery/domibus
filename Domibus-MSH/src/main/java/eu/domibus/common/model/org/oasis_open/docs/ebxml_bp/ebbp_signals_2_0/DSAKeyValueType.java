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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DSAKeyValueType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="DSAKeyValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="P" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *           &lt;element name="Q" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;/sequence>
 *         &lt;element name="G" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary" minOccurs="0"/>
 *         &lt;element name="Y" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;element name="J" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary" minOccurs="0"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="Seed" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *           &lt;element name="PgenCounter" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DSAKeyValueType", namespace = "http://www.w3.org/2000/09/xmldsig#", propOrder = {
        "p",
        "q",
        "g",
        "y",
        "j",
        "seed",
        "pgenCounter"
})
public class DSAKeyValueType {

    @XmlElement(name = "P", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected byte[] p;
    @XmlElement(name = "Q", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected byte[] q;
    @XmlElement(name = "G", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected byte[] g;
    @XmlElement(name = "Y", namespace = "http://www.w3.org/2000/09/xmldsig#", required = true)
    protected byte[] y;
    @XmlElement(name = "J", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected byte[] j;
    @XmlElement(name = "Seed", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected byte[] seed;
    @XmlElement(name = "PgenCounter", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected byte[] pgenCounter;

    /**
     * Gets the value of the p property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getP() {
        return this.p;
    }

    /**
     * Sets the value of the p property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setP(final byte[] value) {
        this.p = value;
    }

    /**
     * Gets the value of the q property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getQ() {
        return this.q;
    }

    /**
     * Sets the value of the q property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setQ(final byte[] value) {
        this.q = value;
    }

    /**
     * Gets the value of the g property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getG() {
        return this.g;
    }

    /**
     * Sets the value of the g property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setG(final byte[] value) {
        this.g = value;
    }

    /**
     * Gets the value of the y property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getY() {
        return this.y;
    }

    /**
     * Sets the value of the y property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setY(final byte[] value) {
        this.y = value;
    }

    /**
     * Gets the value of the j property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getJ() {
        return this.j;
    }

    /**
     * Sets the value of the j property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setJ(final byte[] value) {
        this.j = value;
    }

    /**
     * Gets the value of the seed property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getSeed() {
        return this.seed;
    }

    /**
     * Sets the value of the seed property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setSeed(final byte[] value) {
        this.seed = value;
    }

    /**
     * Gets the value of the pgenCounter property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getPgenCounter() {
        return this.pgenCounter;
    }

    /**
     * Sets the value of the pgenCounter property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setPgenCounter(final byte[] value) {
        this.pgenCounter = value;
    }

}
