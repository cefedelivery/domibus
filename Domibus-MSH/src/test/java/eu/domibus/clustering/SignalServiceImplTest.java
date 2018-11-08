package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Topic;

/**
 * 
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SignalServiceImplTest {

    @Injectable
    protected JMSManager jmsManager;

    @Injectable
    protected Topic clusterCommandTopic;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;
    
    @Tested
    SignalServiceImpl signalService;

    @Test
    public void testSignalTrustStoreUpdate_NoException_MessageSent(final @Mocked JmsMessage jmsMessage, final @Mocked JMSMessageBuilder messageBuilder, final @Mocked Domain domain) {

        new Expectations(signalService) {{
            signalService.isClusterConfiguration();
            result = true;

            JMSMessageBuilder.create();
            result = messageBuilder;

            messageBuilder.property(Command.COMMAND, Command.RELOAD_TRUSTSTORE);
            result = messageBuilder;

            messageBuilder.property(MessageConstants.DOMAIN, domain.getCode());
            result = messageBuilder;

            messageBuilder.build();
            result = jmsMessage;
        }};

        //tested method
        signalService.signalTrustStoreUpdate(domain);

        new Verifications() {{
            jmsManager.sendMessageToTopic(jmsMessage, clusterCommandTopic, true);
        }};
    }

    @Test
    public void testSignalPModeUpdate_NoException_MessageSent(final @Mocked JmsMessage jmsMessage, final @Mocked JMSMessageBuilder messageBuilder) {

        new Expectations(signalService) {{
            signalService.isClusterConfiguration();
            result = true;

            JMSMessageBuilder.create();
            result = messageBuilder;

            messageBuilder.property(Command.COMMAND, Command.RELOAD_PMODE);
            result = messageBuilder;

            messageBuilder.property(MessageConstants.DOMAIN, domainContextProvider.getCurrentDomain().getCode());
            result = messageBuilder;

            messageBuilder.build();
            result = jmsMessage;
        }};

        //tested method
        signalService.signalPModeUpdate();

        new Verifications() {{
            jmsManager.sendMessageToTopic(jmsMessage, clusterCommandTopic, true);
        }};
    }


    @Test
    public void testSignalLoggingSetLevel_NoException_MessageSent(final @Mocked JmsMessage jmsMessage, final @Mocked JMSMessageBuilder messageBuilder) {
        final String name = "eu.domibus";
        final String level = "INFO";

        new Expectations(signalService) {{
            signalService.isClusterConfiguration();
            result = true;

            JMSMessageBuilder.create();
            result = messageBuilder;

            messageBuilder.property(Command.COMMAND, Command.LOGGING_SET_LEVEL);
            result = messageBuilder;
            messageBuilder.property(CommandProperty.LOG_NAME, name);
            result = messageBuilder;
            messageBuilder.property(CommandProperty.LOG_LEVEL, level);
            result = messageBuilder;

            messageBuilder.build();
            result = jmsMessage;
        }};

        //tested method
        signalService.signalLoggingSetLevel(name, level);

        new Verifications() {{
            jmsManager.sendMessageToTopic(jmsMessage, clusterCommandTopic, true);
        }};
    }

    @Test
    public void testSignalResetLogging_NoException_MessageSent(final @Mocked JmsMessage jmsMessage, final @Mocked JMSMessageBuilder messageBuilder) {

        new Expectations(signalService) {{
            signalService.isClusterConfiguration();
            result = true;

            JMSMessageBuilder.create();
            result = messageBuilder;

            messageBuilder.property(Command.COMMAND, Command.LOGGING_RESET);
            result = messageBuilder;


            messageBuilder.build();
            result = jmsMessage;
        }};

        //tested method
        signalService.signalLoggingReset();

        new Verifications() {{
            jmsManager.sendMessageToTopic(jmsMessage, clusterCommandTopic, true);
        }};
    }

}