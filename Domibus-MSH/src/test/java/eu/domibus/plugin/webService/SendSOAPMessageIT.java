package eu.domibus.plugin.webService;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.AbstractIT;
import eu.domibus.common.validators.XmlValidationEventHandler;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


/**
 * Since we cannot deploy the entire war in Jetty this class does not attempts a real CXF WS validation but simulates it
 * validating a submitted message request with the same schema and then mocking the response of the WS.
 *
 */
public class SendSOAPMessageIT extends AbstractIT {

    protected static final URL WSDL_LOCATION;

    protected static final QName SERVICE = new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1");
    protected static final QName BACKEND_PORT = new QName("http://org.ecodex.backend/1_1/", "BACKEND_PORT");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8123);

    @Autowired
    private MessageFactory messageFactory; // defined in the spring-context.xml

    static {
        Locale.setDefault(Locale.ENGLISH);
        URL url = SendSOAPMessageIT.class.getResource("schemas/BackendService_1_1.wsdl");
        if (url == null) {
            url = SendSOAPMessageIT.class.getClassLoader().getResource("schemas/BackendService_1_1.wsdl");
        }
        if (url == null) {
            Logger.getLogger(SendSOAPMessageIT.class.getName()).log(Level.SEVERE, "Can not initialize the default wsdl from {0}", "schemas/BackendService_1_1.wsdl");
            assert false;
        }
        WSDL_LOCATION = url;
    }

   /*  This is starting Jetty and deploys domibus war but it throws java.lang.OutOfMemoryError: PermGen space.

   protected static Server server;

    @BeforeClass
    public static void setUp() throws Exception {

        server = new Server(8080);

        WebAppContext wactx = new WebAppContext();
        wactx.setContextPath("/domibus");
        wactx.setWar("target/domibus.war");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] {wactx, new DefaultHandler()});
        server.setHandler(handlers);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            server.stop();
            server.destroy();
            server = null;
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (server != null) {
            server.stop();
            server.destroy();
            server = null;
        }
    }*/

    @Test
    public void testSendValidMessage() throws Exception {

        String body = getAS4Response("blue2redMessageResponse.xml");

        stubFor(post(urlEqualTo("/domibus/services/backend"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/soap+xml;charset=UTF-8")
                        .withBody(body)));

        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/as4/blue2redGoodMessage.xml");
        validateXml(is);

        Service jaxwsService = Service.create(WSDL_LOCATION, SERVICE);
        Dispatch<SOAPMessage> dispatcher = jaxwsService.createDispatch(BACKEND_PORT, SOAPMessage.class, Service.Mode.MESSAGE);
        is = getClass().getClassLoader().getResourceAsStream("dataset/as4/blue2redGoodMessage.xml");
        SOAPMessage reqMsg = messageFactory.createMessage(null, is);
        Assert.assertNotNull(reqMsg);
        SOAPMessage soapResponse = dispatcher.invoke(reqMsg);
        Assert.assertNotNull(soapResponse);
        Assert.assertTrue(soapResponse.getSOAPBody().getTextContent().contains("6f6aeccb-04af-404b-b224-0249acf7b562@domibus.eu"));
    }

    @Test
    public void testSendInvalidMessage() throws Exception {

        try {
            //System.out.println("Default locale [" + Locale.getDefault() + "]");
            validateXml(getClass().getClassLoader().getResourceAsStream("dataset/as4/blue2redInvalidMessage.xml"));
        } catch (javax.xml.bind.UnmarshalException soapEx) {
            Assert.assertTrue(soapEx.getCause().getMessage().contains("The content of element 'ns:From' is not complete"));
        }
    }


    private void validateXml(InputStream xml) throws SAXException, JAXBException {

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        StreamSource[] sources = new StreamSource[4];
        sources[0] = new StreamSource(getClass().getClassLoader().getResourceAsStream("schemas/xml.xsd"));
        sources[1] = new StreamSource(getClass().getClassLoader().getResourceAsStream("schemas/xmlmime.xsd"));
        sources[2] = new StreamSource(getClass().getClassLoader().getResourceAsStream("schemas/envelope.xsd"));
        sources[3] = new StreamSource(getClass().getClassLoader().getResourceAsStream("schemas/domibus-header.xsd"));
        Schema xmlSchema = sf.newSchema(sources);

        JAXBContext jaxbContext = JAXBContext.newInstance("eu.domibus.common.model.org.w3._2003._05.soap_envelope");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(xmlSchema);
        unmarshaller.setEventHandler(new XmlValidationEventHandler());
        unmarshaller.unmarshal(xml);
    }
}


