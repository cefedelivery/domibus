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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

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
 *         &lt;element name="bodyload" type="{http://org.ecodex.backend/1_1/}PayloadType" minOccurs="0"/>
 *         &lt;element name="payload" type="{http://org.ecodex.backend/1_1/}PayloadType" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"bodyload", "payload"})
@XmlRootElement(name = "downloadMessageResponse")
public class DownloadMessageResponse {

    protected PayloadType bodyload;
    protected List<PayloadType> payload;

    /**
     * Gets the value of the bodyload property.
     *
     * @return possible object is {@link PayloadType }
     */
    public PayloadType getBodyload() {
        return this.bodyload;
    }

    /**
     * Sets the value of the bodyload property.
     *
     * @param value allowed object is {@link PayloadType }
     */
    public void setBodyload(final PayloadType value) {
        this.bodyload = value;
    }

    /**
     * Gets the value of the payload property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the payload property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPayload().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link PayloadType }
     */
    public List<PayloadType> getPayload() {
        if (this.payload == null) {
            this.payload = new ArrayList<>();
        }
        return this.payload;
    }
}
