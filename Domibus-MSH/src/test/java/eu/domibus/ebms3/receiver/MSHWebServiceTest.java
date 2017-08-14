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
import eu.domibus.common.services.ReliabilityService;
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
import eu.domibus.ebms3.receiver.handler.PullRequestHandler;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
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

    @Injectable
    PullRequestHandler pullRequestHandler;

    @Injectable
    ReliabilityService reliabilityService;


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
            soapRequestMessage.getProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
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
    public void testHandlePullRequest(
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

        final UserMessage userMessage = new MessageTestUtility().createSampleUserMessage();
        final String messageId = userMessage.getMessageInfo().getMessageId();


        new Expectations() {{

            messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
            result = pullContext;


            messageExchangeService.retrieveReadyToPullUserMessageId(pullContext.getMpcQualifiedName(), pullContext.getInitiator());
            result = messageId;

        }};
        SOAPMessage soapMessage = mshWebservice.handlePullRequest(messaging);
        new Verifications() {{
            messageExchangeService.extractProcessOnMpc(mpcQualifiedName);
            times = 1;

            messageExchangeService.retrieveReadyToPullUserMessageId(pullContext.getMpcQualifiedName(), pullContext.getInitiator());
            times = 1;

            pullRequestHandler.handlePullRequest(messageId, pullContext);
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
            reliabilityService.handlePullReceiptReliability(messageId, ReliabilityChecker.CheckResult.OK, ResponseHandler.CheckResult.WARNING, withAny(legConfiguration));
        }};

    }

    @Test
    public void testHandlePullRequestReceiptWithEbmsException(@Mocked final SOAPMessage request,
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
            reliabilityService.handlePullReceiptReliability(messageId, ReliabilityChecker.CheckResult.PULL_FAILED, null, withAny(legConfiguration));
            times = 1;

        }};

    }

    @Test
    public void testHandlePullRequestReceiptWithReliabilityException(@Mocked final SOAPMessage request,
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
            reliabilityService.handlePullReceiptReliability(messageId, ReliabilityChecker.CheckResult.PULL_FAILED, null, withAny(legConfiguration));
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
        messaging.setUserMessage(new MessageTestUtility().createSampleUserMessage());
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






}
