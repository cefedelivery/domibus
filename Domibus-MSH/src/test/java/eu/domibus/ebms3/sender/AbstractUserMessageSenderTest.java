package eu.domibus.ebms3.sender;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.PolicyService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class AbstractUserMessageSenderTest {


    @Tested
    AbstractUserMessageSender abstractUserMessageSender;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected MSHDispatcher mshDispatcher;

    @Injectable
    protected EbMS3MessageBuilder messageBuilder;

    @Injectable
    protected ReliabilityChecker reliabilityChecker;

    @Injectable
    protected ResponseHandler responseHandler;

    @Injectable
    protected MessageAttemptService messageAttemptService;

    @Injectable
    protected MessageExchangeService messageExchangeService;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected ReliabilityService reliabilityService;

    @Injectable
    protected UserMessageLogDao userMessageLogDao;


    private final String messageId = UUID.randomUUID().toString();

    private final String senderName = "domibus-blue";
    private final String receiverName = "domibus-red";
    private final String legConfigurationName = "pushTestcase1tc1Action";
    private final String pModeKey = "toto";
    private final String configPolicy = "tototiti";
    private final ResponseHandler.CheckResult isOk = ResponseHandler.CheckResult.OK;
    static final String POLICIES = "policies/";


    @Test
    public void testSendMessage(@Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration, @Mocked final Policy policy,
                                final @Mocked Party senderParty, final @Mocked Party receiverParty,
                                final @Mocked SOAPMessage soapMessage, final @Mocked SOAPMessage response) throws Exception {

        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;

        MessageAttempt attempt = createMessageAttempt();
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;

        new Expectations(abstractUserMessageSender) {{
            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractUserMessageSenderTest.class);

            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = policy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            senderParty.getName();
            result = senderName;

            abstractUserMessageSender.createSOAPMessage(userMessage, legConfiguration);
            result = soapMessage;

            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            result = response;

            responseHandler.handle(response);
            result = isOk;

            reliabilityChecker.check(soapMessage, response, pModeKey);
            result = reliabilityCheckSuccessful;

        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage);

        new FullVerifications(abstractUserMessageSender) {{
            LegConfiguration legConfigurationActual;
            String receiverPartyNameActual;
            messageExchangeService.verifyReceiverCertificate(legConfigurationActual = withCapture(), receiverPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(receiverName, receiverPartyNameActual);


            String senderPartyNameActual;
            messageExchangeService.verifySenderCertificate(legConfigurationActual = withCapture(), senderPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(senderName, senderPartyNameActual);

            String messageIdActual;
            ReliabilityChecker.CheckResult checkResultActual;
            reliabilityService.handleReliability(messageIdActual = withCapture(), userMessage, checkResultActual = withCapture(), isOk, legConfiguration);
            Assert.assertEquals(messageId, messageIdActual);
            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);

            MessageAttempt attemptActual;
            String attemptErrorActual;
            MessageAttemptStatus attemptStatusActual;
            abstractUserMessageSender.updateAndCreateAttempt(attemptActual = withCapture(), attemptErrorActual = withCapture(), attemptStatusActual = withCapture());
            Assert.assertEquals(attempt.getId(), attemptActual.getId());
            Assert.assertEquals(attemptError, attemptErrorActual);
            Assert.assertEquals(attemptStatus, attemptStatusActual);
        }};
    }

    @Test
    public void testSendMessage_WrongPolicyConfig_Exception(@Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration) throws EbMS3Exception {

        final MessageAttempt attempt = createMessageAttempt();
        final MessageAttemptStatus attemptStatus = MessageAttemptStatus.ERROR;
        final String attemptError = "Policy configuration invalid";
        final ConfigurationException configurationException = new ConfigurationException("policy file not found");

        new Expectations(abstractUserMessageSender) {{
            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractUserMessageSenderTest.class);

            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = configurationException;

        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage);

        new FullVerifications(abstractUserMessageSender) {{
            EbMS3Exception ebMS3ExceptionActual;
            reliabilityChecker.handleEbms3Exception(ebMS3ExceptionActual = withCapture(), messageId);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, ebMS3ExceptionActual.getErrorCode());
            Assert.assertEquals("Policy configuration invalid", ebMS3ExceptionActual.getErrorDetail());
            Assert.assertEquals(MSHRole.SENDING, ebMS3ExceptionActual.getMshRole());

            MessageAttempt attemptActual;
            String attemptErrorActual;
            MessageAttemptStatus attemptStatusActual;
            abstractUserMessageSender.updateAndCreateAttempt(attemptActual = withCapture(), attemptErrorActual = withCapture(), attemptStatusActual = withCapture());
            Assert.assertEquals(attempt.getId(), attemptActual.getId());
            Assert.assertEquals(attemptError, attemptErrorActual);
            Assert.assertEquals(attemptStatus, attemptStatusActual);
        }};
    }

    @Test
    public void testSendMessage_ChainCertificateInvalid_Exception(@Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration,
                                                                  final @Mocked Party senderParty, final @Mocked Party receiverParty) throws Exception {
        final MessageAttempt attempt = createMessageAttempt();
        final MessageAttemptStatus attemptStatus = MessageAttemptStatus.ABORT;
        final String chainExceptionMessage = "certificate invalid";
        final String attemptError = "[" + DomibusErrorCode.DOM_001 + "]:" + chainExceptionMessage;
        final ChainCertificateInvalidException chainCertificateInvalidException = new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage);
        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.ABORT;


        new Expectations(abstractUserMessageSender) {{
            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractUserMessageSenderTest.class);

            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            messageExchangeService.verifyReceiverCertificate(legConfiguration, receiverParty.getName());
            result = chainCertificateInvalidException;
        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage);

        new FullVerifications(abstractUserMessageSender) {{
            String messageIdActual;
            ReliabilityChecker.CheckResult checkResultActual;
            reliabilityService.handleReliability(messageIdActual = withCapture(), userMessage, checkResultActual = withCapture(), isOk, legConfiguration);
            Assert.assertEquals(messageId, messageIdActual);
            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);

            MessageAttempt attemptActual;
            String attemptErrorActual;
            MessageAttemptStatus attemptStatusActual;
            abstractUserMessageSender.updateAndCreateAttempt(attemptActual = withCapture(), attemptErrorActual = withCapture(), attemptStatusActual = withCapture());
            Assert.assertEquals(attempt.getId(), attemptActual.getId());
            Assert.assertEquals(attemptError, attemptErrorActual);
            Assert.assertEquals(attemptStatus, attemptStatusActual);

        }};
    }


    @Test
    public void testSendMessage_UnmarshallingError_Exception(@Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration,
                                                             @Mocked final Policy policy, final @Mocked Party senderParty, final @Mocked Party receiverParty,
                                                             final @Mocked SOAPMessage soapMessage, final @Mocked SOAPMessage response) throws Exception {
        final MessageAttempt attempt = createMessageAttempt();
        final MessageAttemptStatus attemptStatus = MessageAttemptStatus.ERROR;
        final String attemptError = "Problem occurred during marshalling";
        final ResponseHandler.CheckResult isOk = ResponseHandler.CheckResult.UNMARSHALL_ERROR;
        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;

        new Expectations(abstractUserMessageSender) {{
            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractUserMessageSenderTest.class);

            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = policy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            senderParty.getName();
            result = senderName;

            abstractUserMessageSender.createSOAPMessage(userMessage, legConfiguration);
            result = soapMessage;

            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            result = response;

            responseHandler.handle(response);
            result = isOk;
        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage);

        new FullVerifications(abstractUserMessageSender, messageExchangeService, reliabilityChecker) {{
            LegConfiguration legConfigurationActual;
            String receiverPartyNameActual;
            messageExchangeService.verifyReceiverCertificate(legConfigurationActual = withCapture(), receiverPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(receiverName, receiverPartyNameActual);


            String senderPartyNameActual;
            messageExchangeService.verifySenderCertificate(legConfigurationActual = withCapture(), senderPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(senderName, senderPartyNameActual);

            EbMS3Exception ebMS3ExceptionActual;
            reliabilityChecker.handleEbms3Exception(ebMS3ExceptionActual = withCapture(), messageId);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0004, ebMS3ExceptionActual.getErrorCode());
            Assert.assertEquals("Problem occurred during marshalling", ebMS3ExceptionActual.getErrorDetail());
            Assert.assertEquals(MSHRole.SENDING, ebMS3ExceptionActual.getMshRole());

            String messageIdActual;
            ReliabilityChecker.CheckResult checkResultActual;
            reliabilityService.handleReliability(messageIdActual = withCapture(), userMessage, checkResultActual = withCapture(), isOk, legConfiguration);
            Assert.assertEquals(messageId, messageIdActual);
            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);

            MessageAttempt attemptActual;
            String attemptErrorActual;
            MessageAttemptStatus attemptStatusActual;
            abstractUserMessageSender.updateAndCreateAttempt(attemptActual = withCapture(), attemptErrorActual = withCapture(), attemptStatusActual = withCapture());
            Assert.assertEquals(attempt.getId(), attemptActual.getId());
            Assert.assertEquals(attemptError, attemptErrorActual);
            Assert.assertEquals(attemptStatus, attemptStatusActual);

        }};
    }

    @Test
    public void testSendMessage_DispatchError_Exception(@Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration, @Mocked final Policy policy,
                                                        final @Mocked Party senderParty, final @Mocked Party receiverParty,
                                                        final @Mocked SOAPMessage soapMessage) throws Exception {

        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;

        MessageAttempt attempt = createMessageAttempt();
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.ERROR;
        String attemptError = "OutOfMemory occurred while dispatching messages";

        new Expectations(abstractUserMessageSender) {{
            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractUserMessageSenderTest.class);

            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = policy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            senderParty.getName();
            result = senderName;

            abstractUserMessageSender.createSOAPMessage(userMessage, legConfiguration);
            result = soapMessage;

            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            result = new OutOfMemoryError("OutOfMemory occurred while dispatching messages");

        }};

        try {
            //tested method
            abstractUserMessageSender.sendMessage(userMessage);
            Assert.fail("exception expected");
        } catch (Throwable t) {
            Assert.assertTrue(t instanceof OutOfMemoryError);
        }

        new FullVerifications(abstractUserMessageSender, reliabilityService) {{

            String messageIdActual;
            ReliabilityChecker.CheckResult checkResultActual;
            reliabilityService.handleReliability(messageIdActual = withCapture(), userMessage, checkResultActual = withCapture(), isOk, legConfiguration);
            Assert.assertEquals(messageId, messageIdActual);
            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);

            MessageAttempt attemptActual;
            String attemptErrorActual;
            MessageAttemptStatus attemptStatusActual;
            abstractUserMessageSender.updateAndCreateAttempt(attemptActual = withCapture(), attemptErrorActual = withCapture(), attemptStatusActual = withCapture());
            Assert.assertEquals(attempt.getId(), attemptActual.getId());
            Assert.assertEquals(attemptError, attemptErrorActual);
            Assert.assertEquals(attemptStatus, attemptStatusActual);
        }};
    }


    protected MessageAttempt createMessageAttempt() {
        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        return attempt;
    }

}