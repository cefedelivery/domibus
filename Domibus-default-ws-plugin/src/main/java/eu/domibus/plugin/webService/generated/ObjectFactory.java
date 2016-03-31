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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the backend.ecodex.org._1_1 package.
 * <p/>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 */
@SuppressWarnings("ConstantNamingConvention")
@XmlRegistry
public class ObjectFactory {

    private static final QName _ListPendingMessagesRequest_QNAME = new QName("http://org.ecodex.backend/1_1/",
            "listPendingMessagesRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: backend.ecodex.org._1_1
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FaultDetail }
     */
    public FaultDetail createFaultDetail() {
        return new FaultDetail();
    }

    /**
     * Create an instance of {@link SendRequest }
     */
    public SendRequest createSendRequest() {
        return new SendRequest();
    }

    /**
     * Create an instance of {@link PayloadType }
     */
    public PayloadType createPayloadType() {
        return new PayloadType();
    }

    /**
     * Create an instance of {@link SendRequestURL }
     */
    public SendRequestURL createSendRequestURL() {
        return new SendRequestURL();
    }

    /**
     * Create an instance of {@link PayloadURLType }
     */
    public PayloadURLType createPayloadURLType() {
        return new PayloadURLType();
    }

    /**
     * Create an instance of {@link ListPendingMessagesResponse }
     */
    public ListPendingMessagesResponse createListPendingMessagesResponse() {
        return new ListPendingMessagesResponse();
    }

    /**
     * Create an instance of {@link DownloadMessageResponse }
     */
    public DownloadMessageResponse createDownloadMessageResponse() {
        return new DownloadMessageResponse();
    }

    /**
     * Create an instance of {@link SendResponse }
     */
    public SendResponse createSendResponse() {
        return new SendResponse();
    }

    /**
     * Create an instance of {@link DownloadMessageRequest }
     */
    public DownloadMessageRequest createDownloadMessageRequest() {
        return new DownloadMessageRequest();
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Object }{@code >}}
     */
    @XmlElementDecl(namespace = "http://org.ecodex.backend/1_1/", name = "listPendingMessagesRequest")
    public JAXBElement<Object> createListPendingMessagesRequest(final Object value) {
        return new JAXBElement<>(ObjectFactory._ListPendingMessagesRequest_QNAME, Object.class, null, value);
    }
}
