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
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * Java class for Envelope complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="Envelope">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2003/05/soap-envelope}Header" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2003/05/soap-envelope}Body"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Envelope", propOrder = {"header", "body"})
@XmlRootElement(name = "Envelope")
public class Envelope {

    @XmlAnyAttribute
    private final Map<QName, String> otherAttributes = new HashMap<>();
    @XmlElement(name = "Header")
    protected Header header;
    @XmlElement(name = "Body", required = true)
    protected Body body;

    /**
     * Gets the value of the header property.
     *
     * @return possible object is {@link Header }
     */
    public Header getHeader() {
        return this.header;
    }

    /**
     * Sets the value of the header property.
     *
     * @param value allowed object is {@link Header }
     */
    public void setHeader(final Header value) {
        this.header = value;
    }

    /**
     * Gets the value of the body property.
     *
     * @return possible object is {@link Body }
     */
    public Body getBody() {
        return this.body;
    }

    /**
     * Sets the value of the body property.
     *
     * @param value allowed object is {@link Body }
     */
    public void setBody(final Body value) {
        this.body = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed
     * property on this class.
     * <p/>
     * <p/>
     * the map is keyed by the name of the attribute and the value is the string
     * value of the attribute.
     * <p/>
     * the map returned by this method is live, and you can add new attribute by
     * updating the map directly. Because of this design, there's no setter.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return this.otherAttributes;
    }
}
