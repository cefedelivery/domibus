package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
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
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.ws.WebServiceException;
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
    private static final String TEST_RESOURCES_DIR = "./src/test/resources";
    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";
    private static final String LEG_NO_SECNO_SEC_ACTION = "pushNoSecnoSecAction";
    private static final String PUSH_TESTCASE1_TC1ACTION = "pushTestcase1tc1Action";
    private static final String STRING_TYPE = "string";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String FINAL_RECEIPIENT_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    RawEnvelopeLogDao rawEnvelopeLogDao;

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
    MessageExchangeService messageExchangeService;

    @Injectable
    EbMS3MessageBuilder messageBuilder;

    @Injectable
    UserMessageHandlerService userMessageHandlerService;

    @Injectable
    ResponseHandler responseHandler;

    @Injectable
    ReliabilityChecker reliabilityChecker;

    @Tested
    MSHWebservice mshWebservice;

    /**
     * Happy flow unit testing with actual data
     *
     * @throws SOAPException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws JAXBException
     * @throws EbMS3Exception
     * @throws TransformerException
     */
    @Test
    public void testInvoke_tc1Process_HappyFlow() throws SOAPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, TransformerException, IOException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final Messaging messaging = createDummyRequestMessaging();

        new Expectations(mshWebservice) {{
            soapRequestMessage.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            mshWebservice.getMessaging(withAny(soapRequestMessage));
            result = messaging;

            messaging.getSignalMessage();
            result=null;

            userMessageHandlerService.handleNewUserMessage(withEqual(pmodeKey),withEqual(soapRequestMessage), withEqual(messaging),withAny(new UserMessageHandlerContext()));
            result = soapResponseMessage;
        }};

        mshWebservice.invoke(soapRequestMessage);

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
            times = 0;
        }};
    }






















    /**
     * Unit testing with actual data.
     *
     * @param soapHeader
     * @param soapChildElementsIterator
     * @param messagingXml
     * @throws JAXBException
     * @throws SOAPException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */

    @Test
    public void testInvoke_ErrorInNotifyingIncomingMessage(@Injectable final LegConfiguration legConfiguration, @Injectable final Messaging messaging, @Injectable final UserMessage userMessage) throws SOAPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, TransformerException, IOException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(mshWebservice) {{

            userMessageHandlerService.getMessaging(withAny(soapRequestMessage));
            result=messaging;

            messaging.getSignalMessage();
            result=null;

            UserMessageHandlerContext arg = new UserMessageHandlerContext();
            arg.setLegConfiguration(legConfiguration);
            mshWebservice.getMessageHandler();
            result=arg;

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;


            userMessageHandlerService.handleNewUserMessage(withAny(pmodeKey),withAny(soapRequestMessage), withAny(messaging),withAny(arg));
            result=new EbMS3Exception(null,null,null,null);

        }};

        try {
            mshWebservice.invoke(soapRequestMessage);
        } catch (Exception e) {
            Assert.assertTrue("Expecting Webservice exception!", e instanceof WebServiceException);
        }

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
        }};
    }


    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOG.debug("Inside sample PMode configuration");
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Configuration configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
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

    protected Messaging createDummyRequestMessaging() {
        Messaging messaging = new ObjectFactory().createMessaging();
        messaging.setUserMessage(createSampleUserMessage());
        messaging.getUserMessage().getMessageInfo().setMessageId("1234");
        return messaging;
    }

    protected Messaging createPullRequestMessaging() {
        Messaging messaging = new ObjectFactory().createMessaging();
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setMessageInfo(new MessageInfo());
        PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc("MPC");
        signalMessage.setPullRequest(pullRequest);
        messaging.setSignalMessage(signalMessage);
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
        messageProperties.getProperty().add(createProperty("finalRecipient", FINAL_RECEIPIENT_VALUE, STRING_TYPE));
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
