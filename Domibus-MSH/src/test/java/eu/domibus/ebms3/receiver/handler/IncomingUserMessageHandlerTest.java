package eu.domibus.ebms3.receiver.handler;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.TimestampDateFormatter;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.receiver.UserMessageHandlerContext;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.util.MessageUtil;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */

@RunWith(JMockit.class)
public class IncomingUserMessageHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingUserMessageHandlerTest.class);
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
    IncomingUserMessageHandler incomingUserMessageHandler;

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
    public void testInvoke_tc1Process_HappyFlow(@Injectable Messaging messaging) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(incomingUserMessageHandler) {{
            soapRequestMessage.getProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            userMessageHandlerService.handleNewUserMessage(withEqual(pmodeKey), withEqual(soapRequestMessage), withEqual(messaging), withAny(new UserMessageHandlerContext()));
            result = soapResponseMessage;
        }};

        incomingUserMessageHandler.processMessage(soapRequestMessage, messaging);

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
    public void testInvoke_ErrorInNotifyingIncomingMessage(@Injectable final LegConfiguration legConfiguration,
                                                           @Injectable final Messaging messaging,
                                                           @Injectable final UserMessage userMessage) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(incomingUserMessageHandler) {{

            UserMessageHandlerContext userMessageHandlerContext = new UserMessageHandlerContext();
            userMessageHandlerContext.setLegConfiguration(legConfiguration);

            incomingUserMessageHandler.getMessageHandler();
            result = userMessageHandlerContext;

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;


            userMessageHandlerService.handleNewUserMessage(withAny(pmodeKey), withAny(soapRequestMessage), withAny(messaging), withAny(userMessageHandlerContext));
            result = new EbMS3Exception(null, null, null, null);

        }};

        try {
            incomingUserMessageHandler.processMessage(soapRequestMessage, messaging);
        } catch (Exception e) {
            assertTrue("Expecting Webservice exception!", e instanceof WebServiceException);
        }

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
        }};
    }
}
