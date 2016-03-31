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

package eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.*;

/**
 * This element identifies the service that acts on the message. Its actual semantics is beyond the scope of
 * this specification. The designer of the service may be a standards organization, or an individual or
 * enterprise.
 * Examples of the Service element include:
 * {@code <eb:Service>urn:example.org:services:SupplierOrderProcessing</eb:Service>}
 * {@code <eb:Service type="MyServiceTypes">QuoteToCollect</eb:Service>}
 * <p/>
 * When the value of the element is http://docs.oasis-open.org/ebxmlmsg/
 * ebms/v3.0/ns/core/200704/service, then the receiving MSH MUST NOT deliver this
 * message to the Consumer. With the exception of this delivery behavior, and unless indicated otherwise by
 * the eb:Action element, the processing of the message is not different from any other user message
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */

//TODO: respect http://docs.oasis-open.org/ebxmlmsg/ebms/v3.0/ns/core/200704/service
//TODO: throw error if not an uri and type unset

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Service", propOrder = "value")
@Embeddable
public class Service {

    @XmlValue
    @Column(name = "SERVICE_VALUE")
    protected String value = CollaborationInfo.DEFAULT_SERVICE;
    @XmlAttribute(name = "type")
    @Column(name = "SERVICE_TYPE")
    protected String type;

    /**
     * This element identifies the service that acts on the message
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * This element identifies the service that acts on the message
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * The Service element MAY contain a single @type attribute, that indicates how the parties sending and
     * receiving the message will interpret the value of the element. There is no restriction on the value of the
     * type attribute. If the type attribute is not present, the content of the Service element MUST be a URI (see
     * [RFC2396]). If it is not a URI then the MSH MUST report a "ValueInconsistent" error of severity
     * "error".
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return this.type;
    }

    /**
     * The Service element MAY contain a single @type attribute, that indicates how the parties sending and
     * receiving the message will interpret the value of the element. There is no restriction on the value of the
     * type attribute. If the type attribute is not present, the content of the Service element MUST be a URI (see
     * [RFC2396]). If it is not a URI then the MSH MUST report a "ValueInconsistent" error of severity
     * "error".
     *
     * @param value allowed object is {@link String }
     */
    public void setType(final String value) {
        this.type = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;

        final Service service = (Service) o;

        if (this.type != null ? !this.type.equals(service.type) : service.type != null) return false;
        return this.value.equals(service.value);

    }

    @Override
    public int hashCode() {
        int result = this.value.hashCode();
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return result;
    }
}
