package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.MessagingConfiguration;

public interface MultiDomainAlertConfigurationService {

    MessagingConfiguration getMessageCommunicationConfiguration();

}
