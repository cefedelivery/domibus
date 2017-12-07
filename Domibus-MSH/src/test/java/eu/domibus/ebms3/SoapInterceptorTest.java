package eu.domibus.ebms3;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import java.io.InputStream;
import java.security.Security;
import java.util.Properties;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class SoapInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SoapInterceptorTest.class);

    @Injectable
    protected Properties domibusProperties;

    @Before
    public void init() {
        System.setProperty("javax.xml.soap.MessageFactory", "com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    protected Document readDocument(String name) throws XMLStreamException, ParserConfigurationException {
        InputStream inStream = getClass().getClassLoader().getResourceAsStream(name);
        return StaxUtils.read(inStream);
    }

    protected SoapMessage getSoapMessageForDom(Document doc) throws SOAPException {

        SOAPMessage saajMsg = MessageFactory.newInstance().createMessage();
        SOAPPart part = saajMsg.getSOAPPart();
        part.setContent(new DOMSource(doc));
        saajMsg.saveChanges();

        // Create the context map
        MessageImpl message = new MessageImpl();
        message.put(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action");
        message.put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
        SoapMessage msg = new SoapMessage(message);

        Exchange ex = new ExchangeImpl();

        ex.setInMessage(msg);
        msg.setContent(SOAPMessage.class, saajMsg);

        InterceptorChain ic = new PhaseInterceptorChain((new PhaseManagerImpl()).getOutPhases());
        msg.setInterceptorChain(ic);
        ExchangeImpl exchange = new ExchangeImpl();
        Bus bus = new ExtensionManagerBus();
        bus.setExtension(new PolicyBuilderImpl(bus), PolicyBuilder.class);
        exchange.put(Bus.class, bus);
        exchange.put(DispatchClientDefaultProvider.MESSAGE_ID, "123123123");
        exchange.put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
        msg.setExchange(exchange);

        return msg;
    }
}
