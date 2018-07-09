package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class MultiDomainAlertConfigurationServiceImpl implements MultiDomainAlertConfigurationService {

    private final static Logger LOG = LoggerFactory.getLogger(MultiDomainAlertConfigurationServiceImpl.class);

    private static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE = "domibus.alert.msg.communication_failure.active";

    private static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES = "domibus.alert.msg.communication_failure.states";

    private static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL = "domibus.alert.msg.communication_failure.level";

    public static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT = "domibus.alert.msg.communication_failure.mail.subject";

    public static final String DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT = "domibus.alert.cert.expired.mail.subject";

    public static final String DOMIBUS_ALERT_CERT_EXPIRED_LEVEL = "domibus.alert.cert.expired.level";

    public static final String DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS = "domibus.alert.cert.expired.duration_days";

    public static final String DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS = "domibus.alert.cert.expired.frequency_days";

    public static final String DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE = "domibus.alert.cert.expired.active";

    public static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT = "domibus.alert.cert.imminent_expiration.mail.subject";

    public static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL = "domibus.alert.cert.imminent_expiration.level";

    public static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS = "domibus.alert.cert.imminent_expiration.frequency_days";

    public static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS = "domibus.alert.cert.imminent_expiration.delay_days";

    public static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE = "domibus.alert.cert.imminent_expiration.active";

    public static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT = "domibus.alert.user.login_failure.mail.subject";

    public static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL = "domibus.alert.user.login_failure.level";

    public static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE = "domibus.alert.user.login_failure.active";

    public static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT = "domibus.alert.user.account_disabled.subject";

    public static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT = "domibus.alert.user.account_disabled.moment";

    public static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL = "domibus.alert.user.account_disabled.level";

    public static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE = "domibus.alert.user.account_disabled.active";

    public static final String DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME = "domibus.alert.cleaner.alert.lifetime";

    public static final String DOMIBUS_ALERT_SENDER_EMAIL = "domibus.alert.sender.email";

    public static final String DOMIBUS_ALERT_RECEIVER_EMAIL = "domibus.alert.receiver.email";

    public static final String DOMIBUS_ALERT_ACTIVE = "domibus.alert.active";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private ConfigurationLoader<MessagingConfiguration> messagingConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledConfiguration> accountDisabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<LoginFailureConfiguration> loginFailureConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ImminentExpirationCertificateConfiguration> imminentExpirationCertificateConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ExpiredCertificateConfiguration> expiredCertificateConfigurationLoader;

    /**
     * {@inheritDoc}
     */
    @Override
    public MessagingConfiguration getMessageCommunicationConfiguration() {
        return messagingConfigurationLoader.getConfiguration(this::readMessageConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountDisabledConfiguration getAccountDisabledConfiguration() {
        return accountDisabledConfigurationLoader.getConfiguration(this::readAuthenticatorConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginFailureConfiguration getLoginFailureConfigurationLoader() {
        return loginFailureConfigurationLoader.getConfiguration(this::readLoginFailureConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImminentExpirationCertificateConfiguration getImminentExpirationCertificateConfiguration() {
        return imminentExpirationCertificateConfigurationLoader.getConfiguration(this::readImminentExpirationCertificateConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpiredCertificateConfiguration getExpiredCertificateConfiguration() {
        return expiredCertificateConfigurationLoader.getConfiguration(this::readExpiredCertificateConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        switch (alert.getAlertType()) {
            case MSG_COMMUNICATION_FAILURE:
                return getMessageCommunicationConfiguration().getAlertLevel(alert);
            case USER_ACCOUNT_DISABLED:
                return getAccountDisabledConfiguration().getAlertLevel(alert);
            case USER_LOGIN_FAILURE:
                return getLoginFailureConfigurationLoader().getAlertLevel(alert);
            case CERT_IMMINENT_EXPIRATION:
                return getImminentExpirationCertificateConfiguration().getAlertLevel(alert);
            case CERT_EXPIRED:
                return getExpiredCertificateConfiguration().getAlertLevel(alert);
            default:
                LOG.error("Invalid alert type[{}]", alert.getAlertType());
                throw new IllegalArgumentException("Invalid alert type");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMailSubject(AlertType alertType) {
        switch (alertType) {
            case MSG_COMMUNICATION_FAILURE:
                return getMessageCommunicationConfiguration().getMailSubject();
            case USER_ACCOUNT_DISABLED:
                return getAccountDisabledConfiguration().getMailSubject();
            case USER_LOGIN_FAILURE:
                return getLoginFailureConfigurationLoader().getMailSubject();
            case CERT_IMMINENT_EXPIRATION:
                return getImminentExpirationCertificateConfiguration().getMailSubject();
            case CERT_EXPIRED:
                return getExpiredCertificateConfiguration().getMailSubject();
            default:
                LOG.error("Invalid alert type[{}]", alertType);
                throw new IllegalArgumentException("Invalid alert type");
        }
    }

    @Override
    public Integer getAlertLifeTimeInDays(){
        return Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME));
    }

    @Override
    public String  getSendFrom(){
        return domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
    }

    @Override
    public String  getSendTo(){
        return domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);
    }

    @Override
    public Boolean isAlertModuleEnabled() {
        return Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE));
    }

    private MessagingConfiguration readMessageConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            if (!alertActive) {
                return new MessagingConfiguration();
            }
            final Boolean messageAlertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE));
            Map<MessageStatus, AlertLevel> messageStatusLevels = new HashMap<>();
            if (messageAlertActive) {
                final String messageCommunicationStates = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
                final String messageCommunicationLevels = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
                if (StringUtils.isNotEmpty(messageCommunicationStates) && StringUtils.isNotEmpty(messageCommunicationLevels)) {
                    final String[] states = messageCommunicationStates.split(",");
                    final String[] levels = messageCommunicationLevels.split(",");
                    if (states.length == levels.length) {
                        LOG.trace("Each message status has his own level");
                        int i = 0;
                        for (String state : states) {
                            final MessageStatus messageStatus = MessageStatus.valueOf(state);
                            final AlertLevel alertLevel = AlertLevel.valueOf(levels[i++]);
                            messageStatusLevels.put(messageStatus, alertLevel);
                        }
                    } else {
                        final AlertLevel alertLevel = AlertLevel.valueOf(levels[0]);
                        LOG.trace("No one to one mapping between message status and alert level. All message status will have alert level:[{}]", alertLevel);
                        for (String state : states) {
                            final MessageStatus messageStatus = MessageStatus.valueOf(state);
                            messageStatusLevels.put(messageStatus, alertLevel);
                        }
                    }
                }
            }
            LOG.debug("Message communication module active:[{}]", messageAlertActive);
            final String mailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
            MessagingConfiguration messagingConfiguration = new MessagingConfiguration(mailSubject);
            messageStatusLevels.forEach((messageStatus, alertLevel) -> {
                LOG.debug("Watched message status:[{}] with level[{}]", messageStatus, alertLevel);
                messagingConfiguration.addStatusLevelAssociation(messageStatus, alertLevel);

            });
            return messagingConfiguration;
        } catch (Exception ex) {
            LOG.error("Error while configuring message communication alerts for domain:[{}], message alert module will be discarded.", domain, ex);
            return new MessagingConfiguration();
        }

    }

    private AccountDisabledConfiguration readAuthenticatorConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            if (!alertActive) {
                LOG.debug("Alert module is inactive");
                return new AccountDisabledConfiguration(false);
            }

            final Boolean accountDisabledActive = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE));
            final AlertLevel accountDisabledAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL));
            final AccountDisabledMoment accountDisabledMoment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT));
            final String accountDisabledMailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT);

            return new AccountDisabledConfiguration(
                    accountDisabledActive,
                    accountDisabledAlertLevel,
                    accountDisabledMoment,
                    accountDisabledMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading authenticator alert module configuration for domain:[{}], ", domain);
            return new AccountDisabledConfiguration(false);
        }

    }

    private LoginFailureConfiguration readLoginFailureConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            if (!alertActive) {
                LOG.debug("Alert module is inactive");
                return new LoginFailureConfiguration(false);
            }
            final Boolean loginFailureActive = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE));
            final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL));
            final String loginFailureMailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT);

            return new LoginFailureConfiguration(
                    loginFailureActive,
                    loginFailureAlertLevel,
                    loginFailureMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading authenticator alert module configuration for domain:[{}], ", domain);
            return new LoginFailureConfiguration(false);
        }

    }

    private ImminentExpirationCertificateConfiguration readImminentExpirationCertificateConfiguration(Domain domain) {
        final Boolean alertActive = isAlertModuleEnabled();
        if (!alertActive) {
            LOG.debug("Alert module is inactive");
            return new ImminentExpirationCertificateConfiguration(false);
        }
        try {
            final Boolean imminentExpirationActive = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE));
            final Integer imminentExpirationDelay = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS));
            final Integer imminentExpirationFrequency = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS));
            final AlertLevel imminentExpirationAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL));
            final String imminentExpirationMailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);

            return new ImminentExpirationCertificateConfiguration(
                    imminentExpirationActive,
                    imminentExpirationDelay,
                    imminentExpirationFrequency,
                    imminentExpirationAlertLevel,
                    imminentExpirationMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain);
            return new ImminentExpirationCertificateConfiguration(false);
        }

    }

    private ExpiredCertificateConfiguration readExpiredCertificateConfiguration(Domain domain) {
        final Boolean alertActive = isAlertModuleEnabled();
        if (!alertActive) {
            LOG.debug("Alert module is inactive");
            return new ExpiredCertificateConfiguration(false);
        }
        try {
            final Boolean revokedActive = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE));
            final Integer revokedFrequency = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS));
            final Integer revokedDuration = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS));
            final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL));
            final String expiredMailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);

            return new ExpiredCertificateConfiguration(
                    revokedActive,
                    revokedFrequency,
                    revokedDuration,
                    revocationLevel,
                    expiredMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain);
            return new ExpiredCertificateConfiguration(false);
        }
    }

}
