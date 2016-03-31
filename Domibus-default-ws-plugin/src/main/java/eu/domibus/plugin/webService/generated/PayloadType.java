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

import eu.domibus.common.model.org.w3._2005._05.xmlmime.Base64Binary;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p/>
 * Java class for PayloadType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="PayloadType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2005/05/xmlmime>base64Binary">
 *       &lt;attribute name="payloadId" use="required" type="{http://www.w3.org/2001/XMLSchema}token" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PayloadType")
public class PayloadType extends Base64Binary {

    @XmlAttribute(name = "payloadId", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String payloadId;

    /**
     * Gets the value of the payloadId property.
     *
     * @return possible object is {@link String }
     */
    public String getPayloadId() {
        return this.payloadId;
    }

    /**
     * Sets the value of the payloadId property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPayloadId(final String value) {
        this.payloadId = value;
    }
}
