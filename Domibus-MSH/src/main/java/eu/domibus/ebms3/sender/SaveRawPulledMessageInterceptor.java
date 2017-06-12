package eu.domibus.ebms3.sender;

import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.SoapUtil;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceException;

/**
 * Created by dussath on 6/12/17.
 * Interceptor to save the raw xml in case of a pull message with non repudiation setup.
 */
//@thom test this class
public class SaveRawPulledMessageInterceptor extends AbstractSoapInterceptor {

    @Autowired
    private MessageExchangeService messageExchangeService;

    public SaveRawPulledMessageInterceptor() {
        super(Phase.WRITE_ENDING);
        addAfter(SoapOutInterceptor.SoapOutEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Object messageType = message.getExchange().get(MSHDispatcher.MESSAGE_TYPE_OUT);
        Object messageId = message.getExchange().get(MSHDispatcher.MESSAGE_ID);
        if(!MessageType.USER_MESSAGE.equals(messageType) || messageId==null){
            return;
        }
        try {
            SOAPMessage soapContent = message.getContent(SOAPMessage.class);
            String rawXMLMessage = SoapUtil.getRawXMLMessage(soapContent);
            messageExchangeService.savePulledMessageRawXml(rawXMLMessage,messageId.toString());
        } catch (TransformerException e) {
            throw new WebServiceException(new IllegalArgumentException(e));
        }

    }
}