package eu.domibus.ebms3.receiver;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
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

    @WebMethod
    @WebResult(name = "soapMessageResult")
    public SOAPMessage invoke(final SOAPMessage request) {
        LOG.info("-------------------------------------------------invoked");
        try {
            SOAPFactory soapFac = SOAPFactory.newInstance();
            SOAPMessage responseMessage = messageFactory.createMessage();
            QName sayHi = new QName("http://apache.org/hello_world_rpclit", "sayHiWAttach");
            responseMessage.getSOAPBody().addChildElement(soapFac.createElement(sayHi));
            responseMessage.saveChanges();

            LOG.info("Invoke [{}]", request);
            return responseMessage;

//            throw new IllegalArgumentException("my exception");

        } catch (Exception e) {
            throw new WebServiceException(e);
        }

    }


}
