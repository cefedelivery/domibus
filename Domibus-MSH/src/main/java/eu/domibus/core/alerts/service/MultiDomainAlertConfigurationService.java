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
    MessagingModuleConfiguration getMessageCommunicationConfiguration();

    /**
     * @return account disabled module configuration
     */
    AccountDisabledModuleConfiguration getAccountDisabledConfiguration();

    /**
     * @return login failure module configuration
     */
    LoginFailureModuleConfiguration getLoginFailureConfiguration();

    /**
     * @return certificate imminent expiration module configuration
     */
    ImminentExpirationCertificateModuleConfiguration getImminentExpirationCertificateConfiguration();

    /**
     * @return certificate expired module configuration
     */
    ExpiredCertificateModuleConfiguration getExpiredCertificateConfiguration();

    /**
     * @return alert common configuration
     */
    CommonConfiguration getCommonConfiguration();

    /**
     * Return alert level based on alert(type)
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
     * Check if the alert module is enabled.
     *
     * @return whether the module is active or not.
     */
    Boolean isAlertModuleEnabled();

    /**
     * @return alert events module configuration
     */
    AlertEventModuleConfiguration getRepetitiveEventConfiguration(AlertType alertType);

    /**
     * With the introduction of multitenancy, a super user has been created.
     * It has its own property definition for some of the domibus properties.
     * The following methods are helper methods to retrieve super or domain alert property name
     * depending on the context.
     */

    /**
     * @return name for the alert email active property.
     */
    String getSendEmailActivePropertyName();

    /**
     *
     * @return name of the alert retry max attempts property.
     */
    String getAlertRetryMaxAttemptPropertyName();

    /**
     *
     * @return name of the alert time between retry property.
     */
    String getAlertRetryTimePropertyName();

}
