package eu.domibus.core.alerts;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.configuration.MessagingConfiguration;
import eu.domibus.core.alerts.model.Alert;
import eu.domibus.core.alerts.model.AlertLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MultiDomainAlertConfigurationServiceImpl implements MultiDomainAlertConfigurationService{

    private final static Logger LOG = LoggerFactory.getLogger(MultiDomainAlertConfigurationServiceImpl.class);

    public static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE = "domibus.alert.msg.communication_failure.active";

    public static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES = "domibus.alert.msg.communication_failure.states";
    public static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL = "domibus.alert.msg.communication_failure.level";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    private final Map<Domain, MessagingConfiguration> messagingConfigurationMap = new HashMap<>();

    @Override
    public MessagingConfiguration getMessageCommunicationConfiguration(final Domain domain) {
        MessagingConfiguration messagingConfiguration = this.messagingConfigurationMap.get(domain);
        if (messagingConfiguration == null) {
            synchronized (messagingConfigurationMap) {
                messagingConfiguration = this.messagingConfigurationMap.get(domain);
                if(messagingConfiguration==null) {
                    final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
                    messagingConfiguration = new MessagingConfiguration(false);
                    if (alertActive) {
                        messagingConfiguration = readMessageConfiguration(domain);
                    }
                    messagingConfigurationMap.put(domain, messagingConfiguration);
                }
            }
        }
        return messagingConfigurationMap.get(domain);
    }

    private MessagingConfiguration readMessageConfiguration(Domain domain) {
        try {
            final Boolean messageAlertActif = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE));
            Map<MessageStatus, AlertLevel> messageStatusLevels = new HashMap<>();
            if (messageAlertActif) {
                final String messageCommunicationStates = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
                final String messageCommunicationLevels = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
                if (StringUtils.isNotEmpty(messageCommunicationStates) && StringUtils.isNotEmpty(messageCommunicationLevels)) {
                    final String[] states = messageCommunicationStates.split(",");
                    final String[] levels = messageCommunicationStates.split(",");
                    if (states.length == levels.length) {
                        int i = 0;
                        for (String state : states) {
                            final MessageStatus messageStatus = MessageStatus.valueOf(state);
                            final AlertLevel alertLevel = AlertLevel.valueOf(levels[i++]);
                            messageStatusLevels.put(messageStatus, alertLevel);
                        }
                    } else {
                        final AlertLevel alertLevel = AlertLevel.valueOf(levels[0]);
                        for (String state : states) {
                            final MessageStatus messageStatus = MessageStatus.valueOf(state);
                            messageStatusLevels.put(messageStatus, alertLevel);
                        }
                    }
                }
            }
            LOG.debug("Message communication module active:[{}]", messageAlertActif);
            MessagingConfiguration messagingConfiguration = new MessagingConfiguration(messageAlertActif);
            messageStatusLevels.forEach((messageStatus, alertLevel) -> {
                LOG.debug("Watched message status:[{}] with level[{}]", messageStatus, alertLevel);
                messagingConfiguration.addStatusLevelAssociation(messageStatus, alertLevel);

            });
        } catch (Exception ex) {
            LOG.error("Error while configuring message communication alerts for domain:[{}], message alert module will be discarded.", domain, ex);
            return new MessagingConfiguration(false);
        }

    }

}
