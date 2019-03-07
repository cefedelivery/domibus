package eu.domibus.ebms3.sender;

import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.PolicyService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

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

    

    @Test
    public void sendMessage(@Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration, @Mocked final Policy policy) throws Exception {
        final String pModeKey = "toto";
        final String legConfigurationName = "titi";
        final String configPolicy  = "tototiti";


        new Expectations() {{
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

            policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
            result = policy;

        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage);


        new FullVerifications() {{

        }};
    }

    @Test
    public void updateAndCreateAttempt() {
    }
}