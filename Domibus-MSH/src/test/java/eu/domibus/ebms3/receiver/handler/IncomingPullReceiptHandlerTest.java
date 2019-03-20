package eu.domibus.ebms3.receiver.handler;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.UserMessageLogEntity;
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
import eu.domibus.core.pull.PullRequestResult;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
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
public class IncomingPullReceiptHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullReceiptHandlerTest.class);
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
    IncomingPullReceiptHandler incomingPullReceiptHandler;

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
    SoapUtil soapUtil;

    @Test
    public void testHandlePullRequestReceiptHappyFlow(@Mocked final SOAPMessage request,
                                                      @Mocked final Messaging messaging,
                                                      @Mocked final UserMessage userMessage,
                                                      @Mocked final MessageExchangeConfiguration messageConfiguration,
                                                      @Injectable final PullRequestResult pullRequestResult,
                                                      @Injectable final MessagingLock messagingLock,
                                                      @Injectable final SOAPMessage soapMessage,
                                                      @Injectable final LegConfiguration legConfiguration) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        final UserMessageLogEntity userMessageLog = new UserMessageLogEntity();
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
        new Expectations() {{
            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            pullMessageService.getLock(messageId);
            result = messagingLock;

            messagingLock.getMessageState();
            result = MessageState.WAITING;


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

            pullMessageService.updatePullMessageAfterReceipt(ReliabilityChecker.CheckResult.OK, ResponseHandler.CheckResult.WARNING, userMessageLog, legConfiguration, userMessage);
            result = pullRequestResult;
        }};

        incomingPullReceiptHandler.handlePullRequestReceipt(request, messaging);

        new Verifications() {{
            messagingDao.findUserMessageByMessageId(messageId);
            times = 1;
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            times = 1;
            pModeProvider.getLegConfiguration(pModeKey);
            times = 1;
            responseHandler.handle(request);
            times = 1;
            pullMessageService.updatePullMessageAfterReceipt(ReliabilityChecker.CheckResult.OK, ResponseHandler.CheckResult.WARNING, userMessageLog, legConfiguration, userMessage);
            pullMessageService.releaseLockAfterReceipt(pullRequestResult);
        }};

    }


    @Test
    public void testHandlePullRequestReceiptWithEbmsException(@Mocked final SOAPMessage request,
                                                              @Mocked final Messaging messaging,
                                                              @Mocked final UserMessage userMessage,
                                                              @Injectable final PullRequestResult pullRequestResult,
                                                              @Injectable final MessagingLock messagingLock,
                                                              @Injectable final LegConfiguration legConfiguration) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        final UserMessageLogEntity userMessageLog = new UserMessageLogEntity();
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
        new Expectations(incomingPullReceiptHandler) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;

            pullMessageService.getLock(messageId);
            result = messagingLock;

            messagingLock.getMessageState();
            result = MessageState.WAITING;

            incomingPullReceiptHandler.getSoapMessage(messageId, withAny(legConfiguration), withAny(userMessage));
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", messageId, null);
        }};

        incomingPullReceiptHandler.handlePullRequestReceipt(request, messaging);

        new Verifications() {{
            pullMessageService.updatePullMessageAfterReceipt(ReliabilityChecker.CheckResult.PULL_FAILED, null, userMessageLog, legConfiguration, userMessage);
            times = 1;
            pullMessageService.releaseLockAfterReceipt(pullRequestResult);
            times = 1;
        }};

    }


    @Test
    public void testHandlePullRequestReceiptWithReliabilityException(@Mocked final SOAPMessage request,
                                                                     @Mocked final Messaging messaging,
                                                                     @Mocked final UserMessage userMessage,
                                                                     @Mocked final MessageExchangeConfiguration me,
                                                                     @Injectable final SOAPMessage soapMessage,
                                                                     @Injectable final PullRequestResult pullRequestResult,
                                                                     @Injectable final MessagingLock messagingLock,
                                                                     @Injectable final LegConfiguration legConfiguration) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        final UserMessageLogEntity userMessageLog = new UserMessageLogEntity();
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
        new Expectations(incomingPullReceiptHandler) {{
            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            messagingLock.getMessageState();
            result = MessageState.WAITING;

            incomingPullReceiptHandler.getSoapMessage(messageId, withAny(legConfiguration), withAny(userMessage));
            result = new ReliabilityException(DomibusCoreErrorCode.DOM_004, "test");
        }};

        incomingPullReceiptHandler.handlePullRequestReceipt(request, messaging);

        new Verifications() {{
            pullMessageService.updatePullMessageAfterReceipt(ReliabilityChecker.CheckResult.PULL_FAILED, null, userMessageLog, legConfiguration, userMessage);
            times = 1;
            pullMessageService.releaseLockAfterReceipt(pullRequestResult);
            times = 1;
        }};

    }
}
