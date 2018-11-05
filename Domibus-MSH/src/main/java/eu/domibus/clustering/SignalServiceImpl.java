package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.Topic;

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
        if (!isClusterConfiguration()) {
            LOG.debug("not cluster deployment: no need to signal TrustStore Update");
            return;
        }
        // Sends a signal to all the servers from the cluster in order to trigger the refresh of the trust store
        jmsManager.sendMessageToTopic(JMSMessageBuilder.create()
                .property(Command.COMMAND, Command.RELOAD_TRUSTSTORE)
                .property(MessageConstants.DOMAIN, domain.getCode())
                .build(), clusterCommandTopic);
    }

    @Override
    public void signalPModeUpdate() {
        if (!isClusterConfiguration()) {
            LOG.debug("not cluster deployment: no need to signal PMode Update");
            return;
        }
        // Sends a message into the topic queue in order to refresh all the singleton instances of the PModeProvider.
        jmsManager.sendMessageToTopic(JMSMessageBuilder.create()
                .property(Command.COMMAND, Command.RELOAD_PMODE)
                .property(MessageConstants.DOMAIN, domainContextProvider.getCurrentDomain().getCode())
                .build(), clusterCommandTopic);
    }

    @Override
    public void signalLoggingSetLevel(String name, String level) {
        if (!isClusterConfiguration()) {
            LOG.debug("not cluster deployment: no need to signal Logging Set Level");
            return;
        }

        // Sends a signal to all the servers from the cluster in order to trigger the reset of the logging config
        jmsManager.sendMessageToTopic(JMSMessageBuilder.create()
                .property(Command.COMMAND, Command.LOGGING_SET_LEVEL)
                .property(CommandProperty.LOG_NAME, name)
                .property(CommandProperty.LOG_LEVEL, level)
                .build(), clusterCommandTopic, true);
    }

    @Override
    public void signalLoggingReset() {
        if (!isClusterConfiguration()) {
            LOG.debug("not cluster deployment: no need to signal Logging Reset");
            return;
        }
        //Sends a signal to all the servers from the cluster in order to trigger the reset of the logging config
        jmsManager.sendMessageToTopic(JMSMessageBuilder.create()
                .property(Command.COMMAND, Command.LOGGING_RESET)
                .build(), clusterCommandTopic, true);

    }

    protected boolean isClusterConfiguration() {
        return domibusConfigurationService.isClusterDeployment();
    }
}
