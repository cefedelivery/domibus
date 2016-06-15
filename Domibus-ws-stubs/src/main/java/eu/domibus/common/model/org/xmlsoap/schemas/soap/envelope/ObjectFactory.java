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

package eu.domibus.common.model.org.xmlsoap.schemas.soap.envelope;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.xmlsoap.schemas.soap.envelope package.
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

    private static final QName _Fault_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
    private static final QName _Body_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body");
    private static final QName _Envelope_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
    private static final QName _Header_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.xmlsoap.schemas.soap.envelope
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Body }
     */
    public Body createBody() {
        return new Body();
    }

    /**
     * Create an instance of {@link Header }
     */
    public Header createHeader() {
        return new Header();
    }

    /**
     * Create an instance of {@link Envelope }
     */
    public Envelope createEnvelope() {
        return new Envelope();
    }

    /**
     * Create an instance of {@link Fault }
     */
    public Fault createFault() {
        return new Fault();
    }

    /**
     * Create an instance of {@link Detail }
     */
    public Detail createDetail() {
        return new Detail();
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Fault }{@code >}}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Fault")
    public JAXBElement<Fault> createFault(final Fault value) {
        return new JAXBElement<>(ObjectFactory._Fault_QNAME, Fault.class, null, value);
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Body }{@code >}}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Body")
    public JAXBElement<Body> createBody(final Body value) {
        return new JAXBElement<>(ObjectFactory._Body_QNAME, Body.class, null, value);
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Envelope }{@code >}}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Envelope")
    public JAXBElement<Envelope> createEnvelope(final Envelope value) {
        return new JAXBElement<>(ObjectFactory._Envelope_QNAME, Envelope.class, null, value);
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Header }{@code >}}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Header")
    public JAXBElement<Header> createHeader(final Header value) {
        return new JAXBElement<>(ObjectFactory._Header_QNAME, Header.class, null, value);
    }
}
