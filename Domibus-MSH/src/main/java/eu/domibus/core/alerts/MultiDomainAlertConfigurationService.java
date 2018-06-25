package eu.domibus.core.alerts;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.alerts.configuration.MessagingConfiguration;

public interface MultiDomainAlertConfigurationService {
    MessagingConfiguration getMessageCommunicationConfiguration(Domain domain);
}
