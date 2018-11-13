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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Topic;
import java.util.Map;

/**
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
    public void testSignalTrustStoreUpdate_NoException_MessageSent(final @Mocked Domain domain) {

        new Expectations(signalService) {{

        }};

        //tested method
        signalService.signalTrustStoreUpdate(domain);

        new Verifications() {{
            Map<String, Object> commandPropertiesActual;
            signalService.sendMessage(commandPropertiesActual = withCapture());
            Assert.assertNotNull(commandPropertiesActual);
            Assert.assertEquals(Command.RELOAD_TRUSTSTORE, commandPropertiesActual.get(Command.COMMAND));
            Assert.assertEquals(domain.getCode(), commandPropertiesActual.get(MessageConstants.DOMAIN));

        }};
    }

    @Test
    public void testSignalPModeUpdate_NoException_MessageSent() {

        new Expectations(signalService) {{
        }};

        //tested method
        signalService.signalPModeUpdate();

        new Verifications() {{
            Map<String, Object> commandPropertiesActual;
            signalService.sendMessage(commandPropertiesActual = withCapture());
            Assert.assertNotNull(commandPropertiesActual);
            Assert.assertEquals(Command.RELOAD_PMODE, commandPropertiesActual.get(Command.COMMAND));
            Assert.assertEquals(domainContextProvider.getCurrentDomain().getCode(), commandPropertiesActual.get(MessageConstants.DOMAIN));

        }};
    }


    @Test
    public void testSignalLoggingSetLevel_NoException_MessageSent() {
        final String name = "eu.domibus";
        final String level = "INFO";

        new Expectations(signalService) {{
        }};

        //tested method
        signalService.signalLoggingSetLevel(name, level);

        new Verifications() {{
            Map<String, Object> commandPropertiesActual;
            signalService.sendMessage(commandPropertiesActual = withCapture());
            Assert.assertNotNull(commandPropertiesActual);
            Assert.assertEquals(Command.LOGGING_SET_LEVEL, commandPropertiesActual.get(Command.COMMAND));
            Assert.assertEquals(level, commandPropertiesActual.get(CommandProperty.LOG_LEVEL));
            Assert.assertEquals(name, commandPropertiesActual.get(CommandProperty.LOG_NAME));

        }};
    }

    @Test
    public void testSignalResetLogging_NoException_MessageSent() {

        new Expectations(signalService) {{
        }};

        //tested method
        signalService.signalLoggingReset();

        new Verifications() {{
            Map<String, Object> commandPropertiesActual;
            signalService.sendMessage(commandPropertiesActual = withCapture());
            Assert.assertNotNull(commandPropertiesActual);
            Assert.assertEquals(Command.LOGGING_RESET, commandPropertiesActual.get(Command.COMMAND));
        }};
    }


    @Test
    public void testSendMessage_NoException_MessageSent(final @Mocked JMSMessageBuilder jmsMessageBuilder, final @Mocked JmsMessage jmsMessage, final @Mocked Map<String, Object> commandProperties) {

        new Expectations(signalService) {{
            domibusConfigurationService.isClusterDeployment();
            result = true;

            JMSMessageBuilder.create();
            result = jmsMessageBuilder;

            jmsMessageBuilder.properties(commandProperties);
            result = jmsMessageBuilder;

            jmsMessageBuilder.build();
            result = jmsMessage;
        }};

        //tested method
        signalService.sendMessage(commandProperties);

        new Verifications() {{
            jmsManager.sendMessageToTopic(jmsMessage, clusterCommandTopic, true);
        }};
    }

}