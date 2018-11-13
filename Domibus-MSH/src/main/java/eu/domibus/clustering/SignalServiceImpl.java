package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for {@link SignalService}
 * We are using a {@JMS topic} implementation
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Service
public class SignalServiceImpl implements SignalService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalServiceImpl.class);

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    protected Topic clusterCommandTopic;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Override
    public void signalTrustStoreUpdate(Domain domain) {

        Map<String, Object> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.RELOAD_TRUSTSTORE);
        commandProperties.put(MessageConstants.DOMAIN, domain.getCode());

        sendMessage(commandProperties);
    }

    @Override
    public void signalPModeUpdate() {

        Map<String, Object> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.RELOAD_PMODE);
        commandProperties.put(MessageConstants.DOMAIN, domainContextProvider.getCurrentDomain().getCode());

        sendMessage(commandProperties);
    }

    @Override
    public void signalLoggingSetLevel(String name, String level) {

        Map<String, Object> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.LOGGING_SET_LEVEL);
        commandProperties.put(CommandProperty.LOG_NAME, name);
        commandProperties.put(CommandProperty.LOG_LEVEL, level);

        sendMessage(commandProperties);
    }

    @Override
    public void signalLoggingReset() {

        Map<String, Object> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.LOGGING_RESET);

        sendMessage(commandProperties);
    }

    protected void sendMessage(Map<String, Object> commandProperties){
        if (!domibusConfigurationService.isClusterDeployment()) {
            LOG.debug("not cluster deployment: no need to {}", commandProperties.get(Command.COMMAND));
            return;
        }

        JmsMessage jmsMessage = JMSMessageBuilder.create().properties(commandProperties).build();

        // Sends a command message to topic cluster
        jmsManager.sendMessageToTopic(jmsMessage, clusterCommandTopic, true);
    }

}
