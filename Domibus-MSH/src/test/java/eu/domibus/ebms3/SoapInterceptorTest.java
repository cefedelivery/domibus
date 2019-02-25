package eu.domibus.ebms3;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.SoapUtil;
import mockit.Injectable;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.attachment.AttachmentDeserializer;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.saaj.SAAJStreamWriter;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.*;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.staxutils.PartialXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.wss4j.policy.SPConstants;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Collection;

/**
 * @author idragusa
 * @since 4.0
 */
public class SoapInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SoapInterceptorTest.class);

    protected static String MESSAGE_TYPE_OUT_TEST_VALUE = "MESSAGE_TYPE_OUT_TEST_VALUE";

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected SoapUtil soapUtil;


    protected SOAPMessage createSoapMessage(MessageImpl messageImpl) throws SOAPException, IOException, ParserConfigurationException, SAXException, TransformerException {
        SOAPMessage message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

        final Collection<Attachment> attachments = messageImpl.getAttachments();
        for (Attachment attachment : attachments) {
            final AttachmentPart attachmentPart = message.createAttachmentPart(attachment.getDataHandler());

            attachmentPart.setContentId(attachment.getId());
            attachmentPart.setContentType(attachment.getDataHandler().getContentType());//to check
            message.addAttachmentPart(attachmentPart);
        }

        final String soapEnvelopeString = IOUtils.toString(messageImpl.getContent(InputStream.class));
        final SOAPMessage soapMessage = new SoapUtil().createSOAPMessage(soapEnvelopeString);
        final SOAPElement next = (SOAPElement) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        message.getSOAPHeader().addChildElement(next);

        message.saveChanges();
        return message;
    }

    public void testCreateSoapEnvelope() throws Exception {
        InputStream rawInputStream = new FileInputStream(new File("c:/DEV/domibus-tomcat-4.0/domibus/files/temp/2133e4d5-2247-4727-a44a-f07ae8abdec0"));
        MessageImpl messageImpl = new MessageImpl();//"org.apache.cxf.binding.soap.SoapVersion" ->
        messageImpl.setContent(InputStream.class, rawInputStream);
        messageImpl.put(Message.CONTENT_TYPE,
                "multipart/related; type=\"application/soap+xml\"; boundary=\"uuid:df560ff6-f165-4c38-8873-965a3caa48c7\"; start=\"<split.root.message@cxf.apache.org>\"; start-info=\"application/soap+xml\"");
        new AttachmentDeserializer(messageImpl).initializeAttachments();
        createSoapMessage(messageImpl);

    }

    public void testCXF2542() throws Exception {
        InputStream rawInputStream = new FileInputStream(new File("c:/DEV/_work/test-e38f79d9-e3c9-4639-a08a-b8f782c99d44"));
        MessageImpl messageImpl = new MessageImpl();//"org.apache.cxf.binding.soap.SoapVersion" ->
        messageImpl.setContent(InputStream.class, rawInputStream);
        messageImpl.put(Message.CONTENT_TYPE,
                "multipart/related; type=\"application/soap+xml\"; boundary=\"uuid:b5efe608-fa17-48cf-9ddd-e19022a3c3fc\"; start=\"<split.root.message@cxf.apache.org>\"; start-info=\"application/soap+xml\"");
        new AttachmentDeserializer(messageImpl).initializeAttachments();
        System.out.println("Finished deserializing");

        final Collection<Attachment> attachments = messageImpl.getAttachments();
        for (Attachment attachment : attachments) {
            System.out.println("CID:" + attachment.getId());
            System.out.println(attachment.getDataHandler().getDataSource().getInputStream());
            Files.copy(attachment.getDataHandler().getDataSource().getInputStream(), Paths.get("c:/DEV/_work/mytest"));

        }
    }

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
        message.put(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action".replaceAll(":", "_pMK_SEP_"));
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
