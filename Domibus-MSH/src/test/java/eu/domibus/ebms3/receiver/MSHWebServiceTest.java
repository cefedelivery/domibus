package eu.domibus.ebms3.receiver;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;
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
    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";
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

    @Injectable
    ReliabilityMatcher pullReceiptMatcher;

    @Injectable
    ReliabilityMatcher pullRequestMatcher;


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
            result = null;

            userMessageHandlerService.handleNewUserMessage(withEqual(pmodeKey), withEqual(soapRequestMessage), withEqual(messaging), withAny(new UserMessageHandlerContext()));
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
            result = messaging;

            messaging.getSignalMessage();
            result = null;

            UserMessageHandlerContext arg = new UserMessageHandlerContext();
            arg.setLegConfiguration(legConfiguration);
            mshWebservice.getMessageHandler();
            result = arg;

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;


            userMessageHandlerService.handleNewUserMessage(withAny(pmodeKey), withAny(soapRequestMessage), withAny(messaging), withAny(arg));
            result = new EbMS3Exception(null, null, null, null);

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

    @Test
    public void testHandlePullRequestMessageFound(
            @Mocked final PhaseInterceptorChain pi,
            @Mocked final Process process,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final PullContext pullContext) throws EbMS3Exception {
        final String mpcQualifiedName = "defaultMPC";

        Messaging messaging = new Messaging();
        SignalMessage signalMessage = new SignalMessage();
        final PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc(mpcQualifiedName);
        signalMessage.setPullRequest(pullRequest);
        messaging.setSignalMessage(signalMessage);

        final UserMessage userMessage = createSampleUserMessage();
        final String messageId = userMessage.getMessageInfo().getMessageId();


        new Expectations() {{
            legConfiguration.getReliability().isNonRepudiation();
            result = true;

            pullRequestMatcher.matchReliableCallBack(withAny(legConfiguration));
            result = true;

            messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
            result = pullContext;

            pullContext.filterLegOnMpc();
            result = legConfiguration;

            messageExchangeService.retrieveReadyToPullUserMessages(pullContext.getMpcQualifiedName(), pullContext.getResponder());
            result = userMessage;
        }};
        SOAPMessage soapMessage = mshWebservice.handlePullRequest(messaging);
        new Verifications() {{
            messageExchangeService.extractProcessOnMpc(mpcQualifiedName);

            PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
            times = 1;

            PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_ID, messageId);
            times = 1;

            messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
            times = 1;

            reliabilityChecker.handleReliability(messageId, ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK, null, legConfiguration);
            times = 1;

        }};
    }

    @Test
    public void testHandlePullRequestNoMessageFound(@Injectable ReliabilityMatcher pullReceiptMatcher,
                                                    @Injectable ReliabilityMatcher pullRequestMatcher,
                                                    @Mocked final PhaseInterceptorChain pi,
                                                    @Mocked final Process process,
                                                    @Mocked final LegConfiguration legConfiguration,
                                                    @Mocked final PullContext pullContext) throws EbMS3Exception {
        final String mpcQualifiedName = "defaultMPC";

        Messaging messaging = new Messaging();
        SignalMessage signalMessage = new SignalMessage();
        final PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc(mpcQualifiedName);
        signalMessage.setPullRequest(pullRequest);
        messaging.setSignalMessage(signalMessage);

        new Expectations() {{

            messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
            result = pullContext;

            messageExchangeService.retrieveReadyToPullUserMessages(pullContext.getMpcQualifiedName(), pullContext.getResponder());
            result = null;
        }};
        mshWebservice.handlePullRequest(messaging);
        new Verifications() {{
            messageExchangeService.extractProcessOnMpc(mpcQualifiedName);

            SignalMessage signal;
            messageBuilder.buildSOAPMessage(signal = withCapture(), withAny(legConfiguration));
            Assert.assertEquals(1, signal.getError().size());
        }};
    }


    @Test
    public void testHandlePullRequestMessageFoundWithErro(@Injectable ReliabilityMatcher pullReceiptMatcher,
                                                          @Injectable ReliabilityMatcher pullRequestMatcher,
                                                          @Mocked final PhaseInterceptorChain pi,
                                                          @Mocked final Process process,
                                                          @Mocked final LegConfiguration legConfiguration,
                                                          @Mocked final PullContext pullContext) throws EbMS3Exception {
        final String mpcQualifiedName = "defaultMPC";

        Messaging messaging = new Messaging();
        SignalMessage signalMessage = new SignalMessage();
        final PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc(mpcQualifiedName);
        signalMessage.setPullRequest(pullRequest);
        messaging.setSignalMessage(signalMessage);

        final UserMessage userMessage = createSampleUserMessage();
        final String messageId = userMessage.getMessageInfo().getMessageId();
        final EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", messageId, null);


        new Expectations() {{
            messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
            result = pullContext;

            pullContext.filterLegOnMpc();
            result = legConfiguration;

            messageExchangeService.retrieveReadyToPullUserMessages(pullContext.getMpcQualifiedName(), pullContext.getResponder());
            result = userMessage;

            messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
            result = ebMS3Exception;
        }};
        SOAPMessage soapMessage = mshWebservice.handlePullRequest(messaging);
        new Verifications() {{
            messageExchangeService.extractProcessOnMpc(mpcQualifiedName);

            Error error;
            messageBuilder.buildSOAPFaultMessage(error = withCapture());
            error.equals(ebMS3Exception.getFaultInfo());
            times = 1;

            reliabilityChecker.handleReliability(messageId, ReliabilityChecker.CheckResult.FAIL, null, legConfiguration);
            times = 1;

        }};
    }

    @Test
    public void testHandlePullRequestReceiptHappyFlow(@Mocked final SOAPMessage request,
                                                      @Mocked final Messaging messaging,
                                                      @Mocked final UserMessage userMessage,
                                                      @Mocked final MessageExchangeConfiguration messageConfiguration,
                                                      @Injectable final SOAPMessage soapMessage,
                                                      @Injectable final LegConfiguration legConfiguration) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        new Expectations() {{
            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = messageConfiguration;

            messageConfiguration.getPmodeKey();
            result = pModeKey;

            responseHandler.handle(request);
            result = ResponseHandler.CheckResult.WARNING;

            reliabilityChecker.check(withAny(soapMessage), request, pModeKey, pullReceiptMatcher);
            result = ReliabilityChecker.CheckResult.OK;
        }};

        mshWebservice.handlePullRequestReceipt(request, messaging);

        new Verifications() {{
            messagingDao.findUserMessageByMessageId(messageId);
            times = 1;
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            times = 1;
            pModeProvider.getLegConfiguration(pModeKey);
            times = 1;
            responseHandler.handle(request);
            times = 1;
            reliabilityChecker.check(withAny(soapMessage), request, pModeKey, pullReceiptMatcher);
            reliabilityChecker.handleReliability(messageId, ReliabilityChecker.CheckResult.OK, ResponseHandler.CheckResult.WARNING, withAny(legConfiguration));
            messageExchangeService.removeRawMessageIssuedByPullRequest(messageId);
        }};

    }

    @Test
    public void testHandlePullRequestWithEbmsException(@Mocked final SOAPMessage request,
                                                       @Mocked final Messaging messaging,
                                                       @Mocked final UserMessage userMessage,
                                                       @Mocked final MessageExchangeConfiguration me,
                                                       @Injectable final SOAPMessage soapMessage,
                                                       @Injectable final LegConfiguration legConfiguration) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        new Expectations(mshWebservice) {{
            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;
            mshWebservice.getSoapMessage(messageId, withAny(legConfiguration), withAny(userMessage));
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", messageId, null);
        }};

        mshWebservice.handlePullRequestReceipt(request, messaging);

        new Verifications() {{

            reliabilityChecker.check(withAny(soapMessage), request, pModeKey, pullReceiptMatcher);
            times = 0;
            reliabilityChecker.handleReliability(messageId, ReliabilityChecker.CheckResult.FAIL, null, withAny(legConfiguration));
            times = 1;

        }};

    }

    @Test
    public void testHandlePullRequestWithReliabilityException(@Mocked final SOAPMessage request,
                                                              @Mocked final Messaging messaging,
                                                              @Mocked final UserMessage userMessage,
                                                              @Mocked final MessageExchangeConfiguration me,
                                                              @Injectable final SOAPMessage soapMessage,
                                                              @Injectable final LegConfiguration legConfiguration) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        new Expectations(mshWebservice) {{
            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;
            mshWebservice.getSoapMessage(messageId, withAny(legConfiguration), withAny(userMessage));
            result = new ReliabilityException(DomibusCoreErrorCode.DOM_004, "test");
        }};

        mshWebservice.handlePullRequestReceipt(request, messaging);

        new Verifications() {{

            reliabilityChecker.check(withAny(soapMessage), request, pModeKey, pullReceiptMatcher);
            times = 0;
            reliabilityChecker.handleReliability(messageId, ReliabilityChecker.CheckResult.FAIL, null, withAny(legConfiguration));
            times = 1;

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
        final MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId("id123456");
        userMessage.setMessageInfo(messageInfo);
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
