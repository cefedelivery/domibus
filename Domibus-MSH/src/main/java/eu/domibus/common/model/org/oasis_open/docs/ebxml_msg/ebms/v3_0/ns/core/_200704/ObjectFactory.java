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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704 package.
 * <p/>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 *
 * @author Apache CXF
 * @version 1.0
 * @since 3.0
 */

@SuppressWarnings("ConstantNamingConvention")
@XmlRegistry
public class ObjectFactory {

    public static final QName _Messaging_QNAME = new QName(
            "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",
            "Messaging");

    public static final QName _UserMessage_QNAME = new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",
            "UserMessage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Messaging }
     */
    public Messaging createMessaging() {
        return new Messaging();
    }

    /**
     * Create an instance of {@link MessageInfo }
     */
    public MessageInfo createMessageInfo() {
        return new MessageInfo();
    }

    /**
     * Create an instance of {@link Description }
     */
    public Description createDescription() {
        return new Description();
    }

    /**
     * Create an instance of {@link Service }
     */
    public Service createService() {
        return new Service();
    }

    /**
     * Create an instance of {@link PartyId }
     */
    public PartyId createPartyId() {
        return new PartyId();
    }

    /**
     * Create an instance of {@link CollaborationInfo }
     */
    public CollaborationInfo createCollaborationInfo() {
        return new CollaborationInfo();
    }

    /**
     * Create an instance of {@link Schema }
     */
    public Schema createSchema() {
        return new Schema();
    }

    /**
     * Create an instance of {@link To }
     */
    public To createTo() {
        return new To();
    }

    /**
     * Create an instance of {@link PullRequest }
     */
    public PullRequest createPullRequest() {
        return new PullRequest();
    }

    /**
     * Create an instance of {@link AgreementRef }
     */
    public AgreementRef createAgreementRef() {
        return new AgreementRef();
    }

    /**
     * Create an instance of {@link PartProperties }
     */
    public PartProperties createPartProperties() {
        return new PartProperties();
    }

    /**
     * Create an instance of {@link Property }
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link PartyInfo }
     */
    public PartyInfo createPartyInfo() {
        return new PartyInfo();
    }

    /**
     * Create an instance of {@link MessageProperties }
     */
    public MessageProperties createMessageProperties() {
        return new MessageProperties();
    }

    /**
     * Create an instance of {@link Error }
     */
    public Error createError() {
        return new Error();
    }

    /**
     * Create an instance of {@link PayloadInfo }
     */
    public PayloadInfo createPayloadInfo() {
        return new PayloadInfo();
    }

    /**
     * Create an instance of {@link SignalMessage }
     */
    public SignalMessage createSignalMessage() {
        return new SignalMessage();
    }

    /**
     * Create an instance of {@link PartInfo }
     */
    public PartInfo createPartInfo() {
        return new PartInfo();
    }

    /**
     * Create an instance of {@link UserMessage }
     */
    public UserMessage createUserMessage() {
        return new UserMessage();
    }

    /**
     * Create an instance of {@link Receipt }
     */
    public Receipt createReceipt() {
        return new Receipt();
    }

    /**
     * Create an instance of {@link From }
     */
    public From createFrom() {
        return new From();
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Messaging }{@code >}}
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", name = "Messaging")
    public JAXBElement<Messaging> createMessaging(final Messaging value) {
        return new JAXBElement<>(ObjectFactory._Messaging_QNAME, Messaging.class, null, value);
    }

    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", name = "UserMessage")
    public JAXBElement<UserMessage> createUserMessage(final UserMessage value) {
        return new JAXBElement<>(ObjectFactory._UserMessage_QNAME, UserMessage.class, null, value);
    }
}
