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

import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for KeyInfoType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="KeyInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}KeyName"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}KeyValue"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}RetrievalMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}X509Data"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}PGPData"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}SPKIData"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}MgmtData"/>
 *         &lt;any processContents='lax' namespace='##other'/>
 *       &lt;/choice>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyInfoType", namespace = "http://www.w3.org/2000/09/xmldsig#", propOrder = {
        "content"
})
public class KeyInfoType {

    @XmlElementRefs({
            @XmlElementRef(name = "KeyName", namespace = "http://www.w3.org/2000/09/xmldsig#", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "SPKIData", namespace = "http://www.w3.org/2000/09/xmldsig#", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "PGPData", namespace = "http://www.w3.org/2000/09/xmldsig#", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "MgmtData", namespace = "http://www.w3.org/2000/09/xmldsig#", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "X509Data", namespace = "http://www.w3.org/2000/09/xmldsig#", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "RetrievalMethod", namespace = "http://www.w3.org/2000/09/xmldsig#", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "KeyValue", namespace = "http://www.w3.org/2000/09/xmldsig#", type = JAXBElement.class, required = false)
    })
    @XmlMixed
    @XmlAnyElement(lax = true)
    protected List<Object> content;
    @XmlAttribute(name = "Id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the content property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link SPKIDataType }{@code >}
     * {@link JAXBElement }{@code <}{@link PGPDataType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link X509DataType }{@code >}
     * {@link String }
     * {@link Element }
     * {@link Object }
     * {@link JAXBElement }{@code <}{@link RetrievalMethodType }{@code >}
     * {@link JAXBElement }{@code <}{@link KeyValueType }{@code >}
     */
    public List<Object> getContent() {
        if (this.content == null) {
            this.content = new ArrayList<Object>();
        }
        return this.content;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
