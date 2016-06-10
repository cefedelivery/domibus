package eu.domibus.plugin.ws;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.AbstractIT;
import eu.domibus.plugin.webService.generated.BackendInterface;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


/**
 * Created by martifp on 08/06/2016.
 */
public class SendSOAPMessageIT extends AbstractIT {

    protected static final URL WSDL_LOCATION;

    protected static final QName SERVICE = new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1");
    protected static final QName BACKEND_PORT = new QName("http://org.ecodex.backend/1_1/", "BACKEND_PORT");

    @Mock
    BackendInterface backendWebservice;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Autowired
    private MessageFactory messageFactory; // defined in the spring-context.xml

    static {
        URL url = SendSOAPMessageIT.class.getResource("BackendService_1_1.wsdl");
        if (url == null) {
            url = SendSOAPMessageIT.class.getClassLoader().getResource("schemas/BackendService_1_1.wsdl");
        }
        if (url == null) {
            Logger.getLogger(SendSOAPMessageIT.class.getName()).log(java.util.logging.Level.INFO, "Can not initialize the default wsdl from {0}", "BackendService_1_1.wsdl");
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

 /* This tries to mock the backend WS
   @Before
    public void before() {

        SendResponse response = new SendResponse();
        response.getMessageID().add("ABCFEDE123");

        MockitoAnnotations.initMocks(this);
        try {
            Mockito.when(backendWebservice.sendMessage(Mockito.any(SendRequest.class), Mockito.any(Messaging.class))).thenReturn(response);
        } catch (SendMessageFault sendMessageFault) {
            sendMessageFault.printStackTrace();
        }

    }*/


    @Test
    public void testSendValidMessage() throws Exception {

        String body = getAS4Response("blue2redMessageResponse.xml");

        stubFor(post(urlEqualTo("/domibus/services/backend"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/soap+xml;charset=UTF-8")
                        .withBody(body)));

        Service jaxwsService = Service.create(WSDL_LOCATION, SERVICE);
        Dispatch<SOAPMessage> dispatcher = jaxwsService.createDispatch(BACKEND_PORT, SOAPMessage.class, Service.Mode.MESSAGE);
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/as4/blue2redGoodMessage.xml");
        SOAPMessage reqMsg = messageFactory.createMessage(null, is);
        Assert.assertNotNull(reqMsg);
        SOAPMessage soapResponse = dispatcher.invoke(reqMsg);
        Assert.assertNotNull(soapResponse);
        Assert.assertTrue(soapResponse.getSOAPBody().getTextContent().contains("6f6aeccb-04af-404b-b224-0249acf7b562@domibus.eu"));

    }

    @Test
    public void testSendInvalidMessage() throws Exception {

        String body = getAS4Response("blue2redMessageFaultResponse.xml");

        stubFor(post(urlEqualTo("/domibus/services/backend"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/soap+xml;charset=UTF-8")
                        .withBody(body)));

        try {
            Service jaxwsService = Service.create(WSDL_LOCATION, SERVICE);
            Dispatch<SOAPMessage> dispatcher = jaxwsService.createDispatch(BACKEND_PORT, SOAPMessage.class, Service.Mode.MESSAGE);
            InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/as4/blue2redInvalidMessage.xml");
            SOAPMessage reqMsg = messageFactory.createMessage(null, is);
            Assert.assertNotNull(reqMsg);
            SOAPMessage soapResponse = dispatcher.invoke(reqMsg);
            Assert.assertNull(soapResponse);
        } catch (SOAPFaultException soapEx) {
            Assert.assertTrue(soapEx.getMessage().contains("Could not validate soapheader caused by: org.xml.sax.SAXParseException: cvc-complex-type"));
        }
    }

}


