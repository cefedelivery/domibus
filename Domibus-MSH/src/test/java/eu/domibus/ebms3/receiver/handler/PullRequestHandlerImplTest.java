package eu.domibus.ebms3.receiver.handler;

import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.util.MessageAttemptUtil;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.MessageTestUtility;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.RetryService;
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
    MessageAttemptUtil messageAttemptUtil;

    @Injectable
    RetryService retryService;

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

            messageExchangeService.handleReliability(messageId, ReliabilityChecker.CheckResult.FAIL, null, legConfiguration);
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

            messageExchangeService.handleReliability(messageId, ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK, null, legConfiguration);
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


}