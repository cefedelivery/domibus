package eu.domibus.ebms3.common.model.mf;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.domibus.ebms3.common.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@XmlRegistry
public class ObjectFactory {

    public final static QName _MessageFragment_QNAME = new QName("http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/", "MessageFragment");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.domibus.ebms3.common.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MessageFragmentType }
     * 
     */
    public MessageFragmentType createMessageFragmentType() {
        return new MessageFragmentType();
    }

    /**
     * Create an instance of {@link MessageHeaderType }
     * 
     */
    public MessageHeaderType createMessageHeaderType() {
        return new MessageHeaderType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageFragmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/", name = "MessageFragment")
    public JAXBElement<MessageFragmentType> createMessageFragment(MessageFragmentType value) {
        return new JAXBElement<MessageFragmentType>(_MessageFragment_QNAME, MessageFragmentType.class, null, value);
    }
}
