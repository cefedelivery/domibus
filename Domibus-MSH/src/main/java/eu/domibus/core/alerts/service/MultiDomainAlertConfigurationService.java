package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.*;

/**
 * Retrieve the configuration for the different type of alert submodules.
 */
public interface MultiDomainAlertConfigurationService {

    /**
     * The configuration of the messaging alert submodule.
     * @return
     */
    MessagingConfiguration getMessageCommunicationConfiguration();

    AccountDisabledConfiguration getAccountDisabledConfiguration();

    LoginFailureConfiguration getLoginFailureConfigurationLoader();

    ImminentExpirationCertificateConfiguration getImminentExpirationCertificateConfiguration();

    ExpiredCertificateConfiguration getExpiredCertificateConfiguration();
}
