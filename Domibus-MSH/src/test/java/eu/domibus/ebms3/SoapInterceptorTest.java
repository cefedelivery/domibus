package eu.domibus.ebms3;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Injectable;
import org.apache.commons.codec.binary.Base64;
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
import org.apache.wss4j.policy.SPConstants;
import org.junit.Before;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import java.io.InputStream;
import java.security.Security;
import java.util.Properties;

/**
 * @author idragusa
 * @since 4.0
 */
public class SoapInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SoapInterceptorTest.class);

    protected static String MESSAGE_TYPE_OUT_TEST_VALUE = "MESSAGE_TYPE_OUT_TEST_VALUE";

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
        AttachmentPart attachment = saajMsg.createAttachmentPart();
        attachment.setContent(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml");
        attachment.setContentId("sbdh-order");
        saajMsg.addAttachmentPart(attachment);
        saajMsg.setProperty(MSHDispatcher.MESSAGE_TYPE_OUT, MESSAGE_TYPE_OUT_TEST_VALUE);
        saajMsg.saveChanges();

        // Create the context map
        MessageImpl message = new MessageImpl();
        message.put(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action");
        message.put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
        message.put(DispatchClientDefaultProvider.ASYMMETRIC_SIG_ALGO_PROPERTY, SPConstants.SHA256);
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
