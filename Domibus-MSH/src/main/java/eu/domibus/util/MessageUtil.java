package eu.domibus.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.ebms3.common.model.mf.MessageFragmentType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.Iterator;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service
public class MessageUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageUtil.class);

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    @Qualifier("jaxbContextMessageFragment")
    @Autowired
    protected JAXBContext jaxbContextMessageFragment;


    public Messaging getMessaging(final SOAPMessage request) throws SOAPException, JAXBException {
        LOG.debug("Unmarshalling the Messaging instance from the request");
        final Node messagingXml = (Node) request.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        @SuppressWarnings("unchecked") final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }

    public MessageFragmentType getMessageFragment(final SOAPMessage request) {
        try {
            LOG.debug("Unmarshalling the MessageFragmentType instance from the request");
            final Iterator messageFragment = request.getSOAPHeader().getChildElements(eu.domibus.ebms3.common.model.mf.ObjectFactory._MessageFragment_QNAME);
            if (!messageFragment.hasNext()) {
                return null;
            }

            final Node messagingXml = (Node) messageFragment.next();
            final Unmarshaller unmarshaller = jaxbContextMessageFragment.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
            @SuppressWarnings("unchecked") final JAXBElement<MessageFragmentType> root = (JAXBElement<MessageFragmentType>) unmarshaller.unmarshal(messagingXml);
            return root.getValue();
        } catch (SOAPException | JAXBException e) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Not possible to get the MessageFragmentType", e);
        }
    }

    public Messaging getMessage(SOAPMessage request) {
        Messaging messaging;
        try {
            messaging = getMessaging(request);
        } catch (SOAPException | JAXBException e) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Not possible to getMessage", e);
        }
        return messaging;
    }

}
