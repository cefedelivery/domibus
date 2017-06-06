package eu.domibus.util;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Created by dussath on 6/6/17.
 */
public class MessageUtil {

    private MessageUtil() {
    }

    public static Messaging getMessaging(final SOAPMessage request, JAXBContext jaxbContext) throws SOAPException, JAXBException {
        final Node messagingXml = (Node) request.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        @SuppressWarnings("unchecked") final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }

    public static Messaging getMessage(SOAPMessage request,JAXBContext jaxbContext) {
        Messaging messaging;
        try {
            messaging = getMessaging(request,jaxbContext);
        } catch (SOAPException | JAXBException e) {
            throw new RuntimeException(e);
        }
        return messaging;
    }

}
