package eu.domibus.ebms3.common.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704 package.
 * <p>
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
     * @return a new instance of {@link Messaging }
     */
    public Messaging createMessaging() {
        return new Messaging();
    }

    /**
     * Create an instance of {@link MessageInfo }
     * @return a new instance of {@link MessageInfo }
     */
    public MessageInfo createMessageInfo() {
        return new MessageInfo();
    }

    /**
     * Create an instance of {@link Description }
     * @return a new instance of {@link Description }
     */
    public Description createDescription() {
        return new Description();
    }

    /**
     * Create an instance of {@link Service }
     * @return a new instance of {@link Service }
     */
    public Service createService() {
        return new Service();
    }

    /**
     * Create an instance of {@link PartyId }
     * @return a new instance of {@link PartyId }
     */
    public PartyId createPartyId() {
        return new PartyId();
    }

    /**
     * Create an instance of {@link CollaborationInfo }
     * @return a new instance of {@link CollaborationInfo }
     */
    public CollaborationInfo createCollaborationInfo() {
        return new CollaborationInfo();
    }

    /**
     * Create an instance of {@link To }
     * @return a new instance of {@link To }
     */
    public To createTo() {
        return new To();
    }

    /**
     * Create an instance of {@link PullRequest }
     * @return a new instance of {@link PullRequest }
     */
    public PullRequest createPullRequest() {
        return new PullRequest();
    }

    /**
     * Create an instance of {@link AgreementRef }
     * @return a new instance of {@link AgreementRef }
     */
    public AgreementRef createAgreementRef() {
        return new AgreementRef();
    }

    /**
     * Create an instance of {@link PartProperties }
     * @return a new instance of {@link PartProperties }
     */
    public PartProperties createPartProperties() {
        return new PartProperties();
    }

    /**
     * Create an instance of {@link Property }
     * @return a new instance of {@link Property }
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link PartyInfo }
     * @return a new instance of {@link PartyInfo }
     */
    public PartyInfo createPartyInfo() {
        return new PartyInfo();
    }

    /**
     * Create an instance of {@link MessageProperties }
     * @return a new instance of {@link MessageProperties }
     */
    public MessageProperties createMessageProperties() {
        return new MessageProperties();
    }

    /**
     * Create an instance of {@link Error }
     * @return a new instance of {@link Error }
     */
    public Error createError() {
        return new Error();
    }

    /**
     * Create an instance of {@link PayloadInfo }
     * @return a new instance of {@link PayloadInfo }
     */
    public PayloadInfo createPayloadInfo() {
        return new PayloadInfo();
    }

    /**
     * Create an instance of {@link SignalMessage }
     * @return a new instance of {@link SignalMessage }
     */
    public SignalMessage createSignalMessage() {
        return new SignalMessage();
    }

    /**
     * Create an instance of {@link PartInfo }
     * @return a new instance of {@link PartInfo }
     */
    public PartInfo createPartInfo() {
        return new PartInfo();
    }

    /**
     * Create an instance of {@link UserMessage }
     * @return a new instance of {@link UserMessage }
     */
    public UserMessage createUserMessage() {
        return new UserMessage();
    }

    /**
     * Create an instance of {@link Receipt }
     * @return a new instance of {@link Receipt }
     */
    public Receipt createReceipt() {
        return new Receipt();
    }

    /**
     * Create an instance of {@link From }
     * @return a new instance of {@link From }
     */
    public From createFrom() {
        return new From();
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Messaging }{@code >}}
     *
     * @param value the {@link Messaging } value
     *
     * @return a new instance of {@link JAXBElement }{@code <}{@link Messaging }{@code >}}
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
