package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Retrieve the configuration for the different type of alert submodules.
 */
public interface MultiDomainAlertConfigurationService {

    /**
     * @return message communication module configuration
     */
    MessagingConfiguration getMessageCommunicationConfiguration();

    /**
     * @return account disabled module configuration
     */
    AccountDisabledConfiguration getAccountDisabledConfiguration();

    /**
     * @return login failure module configuration
     */
    LoginFailureConfiguration getLoginFailureConfiguration();

    /**
     * @return certificate imminent expiration module configuration
     */
    ImminentExpirationCertificateConfiguration getImminentExpirationCertificateConfiguration();

    /**
     * @return certificate expired module configuration
     */
    ExpiredCertificateConfiguration getExpiredCertificateConfiguration();

    /**
     * Reurn alert level based on alert(type)
     *
     * @param alert the alert.
     * @return the level.
     */
    AlertLevel getAlertLevel(Alert alert);

    /**
     * Return the mail subject base on the alert type.
     *
     * @param alertType the type of the alert.
     * @return the mail subject.
     */
    String getMailSubject(AlertType alertType);

    /**
     * Get the umber of days an alert stays in the system before being removed.
     *
     * @return
     */
    Integer getAlertLifeTimeInDays();

    /**
     * Get send from email.
     *
     * @return email.
     */
    String getSendFrom();

    /**
     * Get send to email.
     *
     * @return email.
     */
    String getSendTo();

    /**
     * Check if the alert module is enabled.
     *
     * @return whether the module is active or not.
     */
    Boolean isAlertModuleEnabled();
}
