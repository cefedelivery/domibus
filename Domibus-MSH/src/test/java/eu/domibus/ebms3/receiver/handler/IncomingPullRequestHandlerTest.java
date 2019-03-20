package eu.domibus.ebms3.receiver.handler;

import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.core.security.AuthorizationService;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.receiver.MessageTestUtility;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.util.MessageUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerFactory;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */

@RunWith(JMockit.class)
public class IncomingPullRequestHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullRequestHandlerTest.class);
    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    IncomingMessageHandlerFactory incomingMessageHandlerFactory;

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
    IncomingPullRequestHandler incomingPullRequestHandler;

    @Injectable
    ReliabilityMatcher pullReceiptMatcher;

    @Injectable
    ReliabilityMatcher pullRequestMatcher;

    @Injectable
    PullRequestHandler pullRequestHandler;

    @Injectable
    ReliabilityService reliabilityService;

    @Injectable
    PullMessageService pullMessageService;

    @Injectable
    MessageUtil messageUtil;

    @Injectable
    AuthorizationService authorizationService;

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
        SOAPMessage soapMessage = incomingPullRequestHandler.handlePullRequest(messaging);
        new Verifications() {{
            messageExchangeService.extractProcessOnMpc(mpcQualifiedName);
            times = 1;

            messageExchangeService.retrieveReadyToPullUserMessageId(pullContext.getMpcQualifiedName(), pullContext.getInitiator());
            times = 1;

            pullRequestHandler.handlePullRequest(messageId, pullContext);
            times = 1;
        }};
    }


}
