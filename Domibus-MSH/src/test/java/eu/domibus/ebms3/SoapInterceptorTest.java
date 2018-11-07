package eu.domibus.ebms3;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Injectable;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.Bus;
import org.apache.cxf.attachment.AttachmentDeserializer;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.*;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.wss4j.policy.SPConstants;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.FileInputStream;
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

    @Test
    public void testDeserialize() throws Exception {
        SOAPMessage saajMsg = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
    }

    @Test
    public void testMimeParser() throws MessagingException {
        final FileDataSource fileDataSource = new FileDataSource(new File("c:/DEV/_work/test-c55a550e-e657-436b-a515-c3ed8dc70796"));
        MimeMultipart part = new MimeMultipart(fileDataSource);
        final int count = part.getCount();
        System.out.println(count);
    }

    @Test
    public void testCXF2542() throws Exception {
        InputStream rawInputStream = new FileInputStream(new File("c:/DEV/_work/test-da97014e-6515-4bcf-bd07-35df52431edb"));
        MessageImpl messageImpl = new MessageImpl();//"org.apache.cxf.binding.soap.SoapVersion" ->
        messageImpl.setContent(InputStream.class, rawInputStream);
        messageImpl.put(Message.CONTENT_TYPE,
                "multipart/related; type=\"application/soap+xml\"; boundary=\"uuid:24e7a9ed-2660-4507-8c6b-196190db8634\"; start=\"<split.root.message@cxf.apache.org>\"; start-info=\"application/soap+xml\"");
        new AttachmentDeserializer(messageImpl).initializeAttachments();
//        InputStream inputStreamWithoutAttachments = message.getContent(InputStream.class);
//        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
//        parser.parse(inputStreamWithoutAttachments, new DefaultHandler());
        System.out.println("Finished deserializing");

        final Collection<Attachment> attachments = messageImpl.getAttachments();
        for (Attachment attachment : attachments) {
            System.out.println("CID:" + attachment.getId());
            System.out.println(attachment.getDataHandler().getDataSource().getInputStream());
            Files.copy(attachment.getDataHandler().getDataSource().getInputStream(), Paths.get("c:/DEV/_work/mytest"));

        }

        /*

        final SoapMessage soapMessage = new SoapMessage(messageImpl);
        final Soap12 soapVersion = Soap12.getInstance();
        soapMessage.setVersion(soapVersion);

        InputStream is = messageImpl.getContent(InputStream.class);

        messageImpl.getAttachments();

        XMLStreamReader xreader = StaxUtils.createXMLStreamReader(is, "UTF-8");
        xreader = StaxUtils.configureReader(xreader, messageImpl);
        messageImpl.setContent(XMLStreamReader.class, xreader);

        XMLStreamReader filteredReader = new PartialXMLStreamReader(xreader, soapVersion.getBody());
        HeadersProcessor processor = new HeadersProcessor(soapVersion);
        Document doc =  processor.process(filteredReader);

        messageImpl.setContent(Node.class, doc);


        SOAPMessage finalSoapMessage =  MessageFactory.newInstance().createMessage();
        messageImpl.setContent(SOAPMessage.class, finalSoapMessage);

        SOAPPart part = finalSoapMessage.getSOAPPart();
        messageImpl.setContent(Node.class, part);
        messageImpl.put(W3CDOMStreamWriter.class, new SAAJStreamWriter(part));
//        message.put(BODY_FILLED_IN, Boolean.FALSE);
        System.out.println(finalSoapMessage.getSOAPHeader());

//        final String rawXMLMessage = SoapUtil.getRawXMLMessage(finalSoapMessage);
//        System.out.println(rawXMLMessage);


//        inputStreamWithoutAttachments.close();
        rawInputStream.close();*/
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
