package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;
/**
 * @author Thomas Dussart
 * @since 4.0
 *
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

    AlertLevel getAlertLevel(Alert alert);

    String getMailSubject(AlertType alertType);

    Integer getAlertLifeTimeInDays();

    String  getSendFrom();

    String  getSendTo();
}
