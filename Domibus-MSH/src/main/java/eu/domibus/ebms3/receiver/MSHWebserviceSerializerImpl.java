package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import org.apache.cxf.message.Message;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;


@WebServiceProvider(portName = "local-msh-dispatch", serviceName = "local-msh-dispatch-service")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHWebserviceSerializerImpl implements Provider<SOAPMessage> {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebserviceSerializerImpl.class);

    @Autowired
    private MessageFactory messageFactory;

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    @WebMethod
    @WebResult(name = "soapMessageResult")
    public SOAPMessage invoke(final SOAPMessage request) {
        LOG.info("-------------------------------------------------invoked");
        try {
            final Messaging messaging = MessageUtil.getMessaging(request, jaxbContext);
            final UserMessage userMessage = messaging.getUserMessage();
            System.out.println(userMessage);
        } catch (SOAPException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }


        final String contentType = LOG.getMDC(Message.CONTENT_TYPE);
        final ContentType parse = ContentType.parse(contentType);
        System.out.println(parse);


        final String myfile = LOG.getMDC("myfile");
        System.out.println(myfile);

        try {
            SOAPFactory soapFac = SOAPFactory.newInstance();
            SOAPMessage responseMessage = messageFactory.createMessage();
            QName sayHi = new QName("http://apache.org/hello_world_rpclit", "sayHiWAttach");
            responseMessage.getSOAPBody().addChildElement(soapFac.createElement(sayHi));
            responseMessage.saveChanges();

            LOG.info("Invoke [{}]", request);
            return responseMessage;

        } catch (Exception e) {
            throw new WebServiceException(e);
        }

    }


}
