package eu.domibus.ebms3.receiver.handler;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.MessageTestUtility;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.RetryService;
import eu.domibus.pki.DomibusCertificateException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PullRequestHandlerImplTest {

    @Injectable
    MessageExchangeService messageExchangeService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    EbMS3MessageBuilder messageBuilder;

    @Injectable
    ReliabilityChecker reliabilityChecker;

    @Injectable
    ReliabilityMatcher pullRequestMatcher;

    @Injectable
    MessageAttemptService messageAttemptService;

    @Injectable
    RetryService retryService;

    @Injectable
    ReliabilityService reliabilityService;

    @Tested
    PullRequestHandler pullRequestHandler;


    @Test
    public void testHandlePullRequestMessageFoundWithErro(
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
        final EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", messageId, null);


        new Expectations() {{

            pullContext.filterLegOnMpc();
            result = legConfiguration;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
            result = ebMS3Exception;
        }};
        pullRequestHandler.handleRequest(messageId, pullContext);
        new Verifications() {{
            eu.domibus.ebms3.common.model.Error error;
            messageBuilder.buildSOAPFaultMessage(error = withCapture());
            error.equals(ebMS3Exception.getFaultInfo());
            times = 1;

            reliabilityService.handlePullReliability(messageId, ReliabilityChecker.CheckResult.PULL_FAILED, null, legConfiguration);
            times = 1;

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

        final UserMessage userMessage = new MessageTestUtility().createSampleUserMessage();
        final String messageId = userMessage.getMessageInfo().getMessageId();


        new Expectations() {{
            legConfiguration.getReliability().isNonRepudiation();
            result = true;

            pullRequestMatcher.matchReliableCallBack(withAny(legConfiguration));
            result = true;

            pullContext.filterLegOnMpc();
            result = legConfiguration;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;
        }};
        pullRequestHandler.handleRequest(messageId, pullContext);
        new Verifications() {{

            PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
            times = 1;

            PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_ID, messageId);
            times = 1;

            messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
            times = 1;

            reliabilityService.handlePullReliability(messageId, ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK, null, legConfiguration);
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

        pullRequestHandler.notifyNoMessage(pullContext);
        new Verifications() {{

            SignalMessage signal;
            messageBuilder.buildSOAPMessage(signal = withCapture(), withAny(legConfiguration));
            Assert.assertEquals(1, signal.getError().size());
        }};
    }

    @Test
    public void testHandlePullRequestWithInvalidSenderCertificate(
            @Mocked final PhaseInterceptorChain pi,
            @Mocked final Process process,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final PullContext pullContext) throws EbMS3Exception {

        final String messageId = "whatEverId";

        new Expectations() {{

            messageExchangeService.verifySenderCertificate(legConfiguration, pullContext.getResponder().getName());
            result = new DomibusCertificateException("test");


        }};
        pullRequestHandler.handleRequest(messageId, pullContext);
        new Verifications() {{
            EbMS3Exception e = null;
            reliabilityChecker.handleEbms3Exception(e = withCapture(), messageId);
            times = 1;
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0101, e.getErrorCode());
            Error faultInfo = null;
            messageBuilder.buildSOAPFaultMessage(faultInfo = withCapture());
            times = 1;
            Assert.assertEquals("EBMS:0101", faultInfo.getErrorCode());
            reliabilityService.handlePullReliability(messageId, ReliabilityChecker.CheckResult.PULL_FAILED, null, legConfiguration);
            times = 1;
            MessageAttempt attempt = null;
            messageAttemptService.create(withAny(attempt));
            times = 1;


        }};
    }

    @Test
    public void testHandlePullRequestConfigurationException(
            @Mocked final PhaseInterceptorChain pi,
            @Mocked final Process process,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final PullContext pullContext) throws EbMS3Exception {

        final String messageId = "whatEverId";

        new Expectations() {{

            messageExchangeService.verifySenderCertificate(legConfiguration, pullContext.getResponder().getName());
            result = new ConfigurationException();


        }};
        pullRequestHandler.handleRequest(messageId, pullContext);
        new Verifications() {{
            EbMS3Exception e = null;
            reliabilityChecker.handleEbms3Exception(e = withCapture(), messageId);
            times = 1;
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getErrorCode());
            Error faultInfo = null;
            messageBuilder.buildSOAPFaultMessage(faultInfo = withCapture());
            times = 1;
            Assert.assertEquals("EBMS:0010", faultInfo.getErrorCode());
            reliabilityService.handlePullReliability(messageId, ReliabilityChecker.CheckResult.PULL_FAILED, null, legConfiguration);
            times = 1;
            MessageAttempt attempt = null;
            messageAttemptService.create(withAny(attempt));
            times = 1;


        }};
    }

    @Test
    public void testHandlePullRequestWithInvalidReceiverCertificate(
            @Mocked final PhaseInterceptorChain pi,
            @Mocked final Process process,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final PullContext pullContext) throws EbMS3Exception {

        final String messageId = "whatEverID";
        new Expectations() {{

            messageExchangeService.verifyReceiverCerficate(legConfiguration, pullContext.getInitiator().getName());
            result = new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, "invalid certificate");


        }};
        pullRequestHandler.handleRequest(messageId, pullContext);
        new Verifications() {{
            retryService.purgeTimedoutMessage(messageId);
            times = 1;
            messageBuilder.buildSOAPFaultMessage(withAny(new Error()));
            times = 0;
            reliabilityService.handlePullReliability(messageId, ReliabilityChecker.CheckResult.SEND_FAIL, null, legConfiguration);
            times = 0;
            MessageAttempt attempt = null;
            messageAttemptService.create(withAny(attempt));
            times = 0;


        }};
    }


}