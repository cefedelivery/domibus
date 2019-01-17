package eu.domibus.plugin.jms.xml;

import javax.xml.bind.annotation.XmlRegistry;

@SuppressWarnings("ConstantNamingConvention")
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704
     */
    public ObjectFactory() {
    }

    /**
     * /**
     * Create an instance of {@link Description }
     *
     * @return a new instance of {@link Description }
     */
    public Description createDescription() {
        return new Description();
    }
}
