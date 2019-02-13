package eu.domibus.plugin.jms;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.jms.xml.Description;
import eu.domibus.plugin.jms.xml.ObjectFactory;
import eu.domibus.plugin.transformer.OutgoingMessageTransformer;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class JMSOutgoingMessageTransformer implements OutgoingMessageTransformer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSOutgoingMessageTransformer.class);

    @Autowired
    @Qualifier(value = "jmsJaxbContext")
    private JAXBContext jaxbContext;

    @Override
    public void transformOutgoingMessage(SOAPMessage message) {

        try {
            SOAPElement security = message.getSOAPHeader().addChildElement(WSConstants.WSSE_LN, "wsse", WSConstants.WSSE_NS);

            final Description description = new ObjectFactory().createDescription();
            description.setValue("my desc");
            description.setLang("en");
            this.jaxbContext.createMarshaller().marshal(description, security);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
    }
}
