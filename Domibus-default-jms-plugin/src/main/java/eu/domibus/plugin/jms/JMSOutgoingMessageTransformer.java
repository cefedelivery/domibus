package eu.domibus.plugin.jms;

import eu.domibus.plugin.jms.xml.Description;
import eu.domibus.plugin.jms.xml.ObjectFactory;
import eu.domibus.plugin.transformer.OutgoingMessageTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class JMSOutgoingMessageTransformer implements OutgoingMessageTransformer {

    @Autowired
    @Qualifier(value = "jmsJaxbContext")
    private JAXBContext jaxbContext;

    @Override
    public void transformOutgoingMessage(SOAPMessage message) {

        try {
            final Description description = new ObjectFactory().createDescription();
            description.setValue("my desc");
            description.setLang("en");
            this.jaxbContext.createMarshaller().marshal(description, message.getSOAPHeader());
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
    }
}
