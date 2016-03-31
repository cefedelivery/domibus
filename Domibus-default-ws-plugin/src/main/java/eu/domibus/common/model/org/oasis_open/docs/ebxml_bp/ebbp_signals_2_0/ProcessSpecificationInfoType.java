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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * This defines the content structure for identifying the
 * root ProcessSpecification for which this business signal is being sent. "instanceVersion"
 * attribute refers to the user-defined version of the ProcessSpecification identified by the "name"
 * attribute. The "name" attribute is set to the same value as name
 * attribute for the corresponding ProcessSpecification element within the
 * Business Process Specification instance (For example, the @name attribute of "name" attributeGroup in ebBP). The "xlink:type" attribute has a FIXED
 * value of "simple". This identifies the element as being an XLINK simple link. The
 * "xlink:href" attribute has a value that is a URI that conforms to [RFC2396].
 * It identifies the location of the Business Process Specification instance document that defines the
 * Business Collaboration. The "uuid" attribute captures the unique identifier given to
 * the Business Process Specification instance document that is being referred. It corresponds to the
 * uuid attribute of "ProcessSpecification" element in the Business Process Specification instance document.
 * <p/>
 * <p/>
 * <p>Java class for ProcessSpecificationInfoType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ProcessSpecificationInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}xlink.grp"/>
 *       &lt;attribute name="instanceVersion" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="uuid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessSpecificationInfoType", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
public class ProcessSpecificationInfoType {

    @XmlAttribute(name = "instanceVersion")
    protected String instanceVersion;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "uuid", required = true)
    protected String uuid;
    @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String href;

    /**
     * Gets the value of the instanceVersion property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getInstanceVersion() {
        return this.instanceVersion;
    }

    /**
     * Sets the value of the instanceVersion property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInstanceVersion(final String value) {
        this.instanceVersion = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the uuid property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * Sets the value of the uuid property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUuid(final String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getType() {
        if (this.type == null) {
            return "simple";
        } else {
            return this.type;
        }
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

    /**
     * Gets the value of the href property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getHref() {
        return this.href;
    }

    /**
     * Sets the value of the href property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHref(final String value) {
        this.href = value;
    }

}
