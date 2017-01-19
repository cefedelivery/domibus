package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Arun Raj
 * @since 3.3
 */

@RunWith(JMockit.class)
public class MSHWebServiceTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebServiceTest.class);
    private static final String VALID_PMODE_CONFIG_URI = "SamplePModes/domibus-configuration-valid.xml";
    private static final String LEG_NO_SECNO_SEC_ACTION = "pushNoSecnoSecAction";
    private static final String PUSH_TESTCASE1_TC1ACTION = "pushTestcase1tc1Action";
    private static final String STRING_TYPE = "string";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";


    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    MessagingService messagingService;

    @Injectable
    SignalMessageDao signalMessageDao;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    MessageFactory messageFactory;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    JAXBContext jaxbContext;

    @Injectable
    TransformerFactory transformerFactory;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    TimestampDateFormatter timestampDateFormatter;

    @Injectable
    CompressionService compressionService;

    @Injectable
    MessageIdGenerator messageIdGenerator;

    @Injectable
    PayloadProfileValidator payloadProfileValidator;

    @Injectable
    PropertyProfileValidator propertyProfileValidator;

    @Injectable
    CertificateService certificateService;

    @Injectable
    SOAPMessage soapRequestMessage;

    @Injectable
    SOAPMessage soapResponseMessage;

    @Injectable
    Configuration configuration;

    @Injectable
    LegConfiguration legConfiguration;

    @Tested
    MSHWebservice mshWebservice;

    @Test
    public void testInvoke_tc1Process() throws SOAPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, TransformerException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);
        final Messaging messaging = createSampleMessaging();
        final UserMessage userMessage = messaging.getUserMessage();
        final Party receiverParty = getPartyFromConfiguration(configuration, RED);

        new Expectations(mshWebservice) {{
            soapRequestMessage.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            pModeProvider.getLegConfiguration(withSubstring(PUSH_TESTCASE1_TC1ACTION));
            result = legConfiguration;

            mshWebservice.getMessaging(withAny(soapRequestMessage));
            result = messaging;

            userMessageLogDao.findByMessageId(anyString, MSHRole.RECEIVING);
            result = null;

            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            result = any;

            compressionService.handleDecompression(userMessage, legConfiguration);
            result = true;

            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            result = soapResponseMessage;
        }};

        System.out.println("At point 0");
        mshWebservice.invoke(soapRequestMessage);
        System.out.println("At point END");

        new Verifications() {{
            mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);
        }};
    }


    @Test
    public void testGenerateReceipt() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, ParserConfigurationException {

        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);

        new Expectations(mshWebservice) {{
            messageIdGenerator.generateMessageId();
            result = new MessageIdGenerator().generateMessageId();

            mshWebservice.saveResponse(withAny(soapResponseMessage));
            result = any;
        }};

        System.out.println("At point 100");
        mshWebservice.generateReceipt(soapRequestMessage, legConfiguration, false);
        System.out.println("At point END");
    }


    @Test
    public void testSaveResponse() throws SOAPException, ParserConfigurationException, JAXBException {

        final Messaging messaging = createSampleMessaging();
        final SignalMessage signalMessage = new SignalMessage();
        messaging.setSignalMessage(signalMessage);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        jaxbContext.createMarshaller().marshal(messaging, doc);
        final Node messagingNode = doc.getFirstChild();
        System.out.println("At point 200");

        new Expectations() {{
            System.out.println("At point 201");
            soapResponseMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
            result = messagingNode;

            messaging.getSignalMessage();
            result = signalMessage;
        }};

        System.out.println("At point 202");
        mshWebservice.saveResponse(soapResponseMessage);
    }


    @Test
    public void testCheckCharset_InvalidCharset() {
        final Messaging messaging = new Messaging();
        UserMessage userMessage = new UserMessage();
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();

        PartProperties partProperties = new PartProperties();
        partProperties.getProperties().add(createProperty("CharacterSet", "!#InvalidCharSet", STRING_TYPE));
        partInfo.setPartProperties(partProperties);

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        messaging.setUserMessage(userMessage);

        try {
            mshWebservice.checkCharset(messaging);
            Assert.fail("EBMS3Exception was expected!!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }
    }

    @Test
    public void testSOAPMessageCreation() throws SOAPException, IOException, ParserConfigurationException, SAXException, JAXBException {

////        String soapMessageXML = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:ns=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\" xmlns:_1=\"http://org.ecodex.backend/1_1/\" xmlns:xm=\"http://www.w3.org/2005/05/xmlmime\"> <soap:Header> <ns:Messaging> <ns:UserMessage> <ns:PartyInfo> <ns:From> <ns:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-blue</ns:PartyId> <ns:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator</ns:Role> </ns:From> <ns:To> <ns:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-red</ns:PartyId> <ns:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder</ns:Role> </ns:To> </ns:PartyInfo> <ns:CollaborationInfo> <ns:Service type=\"tc1\">bdx:noprocess</ns:Service> <ns:Action>TC1Leg1</ns:Action> </ns:CollaborationInfo> <ns:MessageProperties> <ns:Property name=\"originalSender\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1</ns:Property> <ns:Property name=\"finalRecipient\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4</ns:Property> </ns:MessageProperties> <ns:PayloadInfo> <ns:PartInfo href=\"cid:message\"> <ns:PartProperties> <ns:Property name=\"MimeType\">text/xml</ns:Property> </ns:PartProperties> </ns:PartInfo> </ns:PayloadInfo> </ns:UserMessage> </ns:Messaging> </soap:Header> <soap:Body> <_1:sendRequest> <payload payloadId=\"cid:message\">PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=</payload> </_1:sendRequest> </soap:Body> </soap:Envelope>";
//        String soapMessageXML = "<soap:Header xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"> <ns:Messaging xmlns:ns=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\"> <ns:UserMessage> <ns:PartyInfo> <ns:From> <ns:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-blue</ns:PartyId> <ns:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator</ns:Role> </ns:From> <ns:To> <ns:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-red</ns:PartyId> <ns:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder</ns:Role> </ns:To> </ns:PartyInfo> <ns:CollaborationInfo> <ns:Service type=\"tc1\">bdx:noprocess</ns:Service> <ns:Action>TC1Leg1</ns:Action> </ns:CollaborationInfo> <ns:MessageProperties> <ns:Property name=\"originalSender\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1</ns:Property> <ns:Property name=\"finalRecipient\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4</ns:Property> </ns:MessageProperties> <ns:PayloadInfo> <ns:PartInfo href=\"cid:message\"> <ns:PartProperties> <ns:Property name=\"MimeType\">text/xml</ns:Property> </ns:PartProperties> </ns:PartInfo> </ns:PayloadInfo> </ns:UserMessage> </ns:Messaging> </soap:Header>";
//        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        Document document =  documentBuilder.parse(new ByteArrayInputStream(soapMessageXML.getBytes()));
//
//
//        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
//        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
//        SOAPHeaderElement soapHeaderElement = soapHeader.addHeaderElement(ObjectFactory._UserMessage_QNAME);
//        soapHeaderElement.addChildElement((SOAPElement) document.getDocumentElement());


//        SOAPElement soapElement = SOAPFactory.newInstance().createElement();

// Use default mapping between Java objects and Soap elements

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        final Messaging messaging = createSampleMessaging();
        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        jaxbContext.createMarshaller().marshal(messaging, doc);
        Node messagingNode = doc.getFirstChild();
        System.out.println(messagingNode.getFirstChild().getFirstChild().getNodeName());

    }

    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOG.debug("Inside sample PMode configuration");
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
        Method m = configuration.getClass().getDeclaredMethod("preparePersist", null);
        m.setAccessible(true);
        m.invoke(configuration);

        return configuration;
    }


    public LegConfiguration getLegFromConfiguration(Configuration configuration, String legName) {
        LegConfiguration result = null;
        for (LegConfiguration legConfiguration1 : configuration.getBusinessProcesses().getLegConfigurations()) {
            if (StringUtils.equalsIgnoreCase(legName, legConfiguration1.getName())) {
                result = legConfiguration1;
            }
        }
        return result;
    }

    public Party getPartyFromConfiguration(Configuration configuration, String partyName) {
        Party result = null;
        for (Party party : configuration.getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(partyName, party.getName())) {
                result = party;
            }
        }
        return result;
    }


    protected Messaging createSampleMessaging() {
        Messaging messaging = new ObjectFactory().createMessaging();
        messaging.setUserMessage(createSampleUserMessage());
        messaging.getUserMessage().getMessageInfo().setMessageId("1234");
        return messaging;
    }

    protected UserMessage createSampleUserMessage() {
        UserMessage userMessage = new UserMessage();
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("TC1Leg1");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setValue("");
        collaborationInfo.setAgreementRef(agreementRef);
        Service service = new Service();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
        messageProperties.getProperty().add(createProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4", STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);

        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");

        PartyId sender = new PartyId();
        sender.setValue(BLUE);
        sender.setType(DEF_PARTY_TYPE);
        from.getPartyId().add(sender);
        partyInfo.setFrom(from);

        To to = new To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");

        PartyId receiver = new PartyId();
        receiver.setValue(RED);
        receiver.setType(DEF_PARTY_TYPE);
        to.getPartyId().add(receiver);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:message");

        PartProperties partProperties = new PartProperties();
        partProperties.getProperties().add(createProperty("text/xml", "MimeType", STRING_TYPE));
        partInfo.setPartProperties(partProperties);

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        return userMessage;
    }

    protected Property createProperty(String name, String value, String type) {
        Property aProperty = new Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }
}
