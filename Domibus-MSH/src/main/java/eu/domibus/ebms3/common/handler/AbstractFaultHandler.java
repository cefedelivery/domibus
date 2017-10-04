package eu.domibus.ebms3.common.handler;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Generic handler for SOAP Faults in context of ebMS3
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public abstract class AbstractFaultHandler implements SOAPHandler<SOAPMessageContext> {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractFaultHandler.class);

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    /**
     * This method extracts a ebMS3 messaging header {@link Messaging} from a {@link javax.xml.soap.SOAPMessage}
     *
     * @param soapMessage
     * @return
     */
    protected Messaging extractMessaging(final SOAPMessage soapMessage) {
        Messaging messaging = null;
        try {
            messaging = ((JAXBElement<Messaging>) this.jaxbContext.createUnmarshaller().unmarshal((Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next())).getValue();
        } catch (JAXBException | SOAPException e) {
            //TODO: make nice
            AbstractFaultHandler.LOG.error("Error extracting messaging object", e);
        }

        return messaging;
    }
}
