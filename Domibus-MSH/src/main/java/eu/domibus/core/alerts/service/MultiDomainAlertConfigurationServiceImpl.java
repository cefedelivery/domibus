package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.AbstractMap;
import java.util.stream.IntStream;


/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class MultiDomainAlertConfigurationServiceImpl implements MultiDomainAlertConfigurationService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(MultiDomainAlertConfigurationServiceImpl.class);

    static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE = "domibus.alert.msg.communication_failure.active";

    static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES = "domibus.alert.msg.communication_failure.states";

    static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL = "domibus.alert.msg.communication_failure.level";

    static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT = "domibus.alert.msg.communication_failure.mail.subject";

    static final String DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT = "domibus.alert.cert.expired.mail.subject";

    static final String DOMIBUS_ALERT_CERT_EXPIRED_LEVEL = "domibus.alert.cert.expired.level";

    static final String DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS = "domibus.alert.cert.expired.duration_days";

    static final String DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS = "domibus.alert.cert.expired.frequency_days";

    static final String DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE = "domibus.alert.cert.expired.active";

    static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT = "domibus.alert.cert.imminent_expiration.mail.subject";

    static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL = "domibus.alert.cert.imminent_expiration.level";

    static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS = "domibus.alert.cert.imminent_expiration.frequency_days";

    static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS = "domibus.alert.cert.imminent_expiration.delay_days";

    static final String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE = "domibus.alert.cert.imminent_expiration.active";

    static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT = "domibus.alert.user.login_failure.mail.subject";

    private static final String DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_MAIL_SUBJECT = "domibus.alert.super.user.login_failure.mail.subject";

    static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL = "domibus.alert.user.login_failure.level";

    private static final String DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_LEVEL = "domibus.alert.super.user.login_failure.level";

    static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE = "domibus.alert.user.login_failure.active";

    private static final String DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_ACTIVE = "domibus.alert.super.user.login_failure.active";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT = "domibus.alert.user.account_disabled.subject";

    private static final String DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_SUBJECT = "domibus.alert.super.user.account_disabled.subject";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT = "domibus.alert.user.account_disabled.moment";

    private static final String DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_MOMENT = "domibus.alert.super.user.account_disabled.moment";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL = "domibus.alert.user.account_disabled.level";

    private static final String DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_LEVEL = "domibus.alert.super.user.account_disabled.level";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE = "domibus.alert.user.account_disabled.active";

    private static final String DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_ACTIVE = "domibus.alert.super.user.account_disabled.active";

    static final String DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME = "domibus.alert.cleaner.alert.lifetime";

    private static final String DOMIBUS_ALERT_SUPER_CLEANER_ALERT_LIFETIME = "domibus.alert.super.cleaner.alert.lifetime";

    static final String DOMIBUS_ALERT_SENDER_EMAIL = "domibus.alert.sender.email";

    private static final String DOMIBUS_ALERT_SUPER_SENDER_EMAIL = "domibus.alert.super.sender.email";

    static final String DOMIBUS_ALERT_RECEIVER_EMAIL = "domibus.alert.receiver.email";

    private static final String DOMIBUS_ALERT_SUPER_RECEIVER_EMAIL = "domibus.alert.super.receiver.email";

    static final String DOMIBUS_ALERT_ACTIVE = "domibus.alert.active";

    private static final String DOMIBUS_ALERT_SUPER_ACTIVE = "domibus.alert.super.active";

    static final String MESSAGE_STATUS_CHANGE_MAIL_SUBJECT = "Message status change";

    static final String ACCOUNT_DISABLED_MAIL_SUBJECT = "Account disabled";

    static final String WHEN_BLOCKED = "WHEN_BLOCKED";

    static final String LOW = "LOW";

    static final String LOGIN_FAILURE_MAIL_SUBJECT = "Login failure";

    static final String CERTIFICATE_IMMINENT_EXPIRATION_MAIL_SUBJECT = "Certificate imminent expiration";

    static final String CERTIFICATE_EXPIRED_MAIL_SUBJECT = "Certificate expired";

    static final String DOMIBUS_ALERT_MAIL_SENDING_ACTIVE = "domibus.alert.mail.sending.active";

    private static final String DOMIBUS_ALERT_SUPER_MAIL_SENDING_ACTIVE = "domibus.alert.super.mail.sending.active";

    static final String DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS = "domibus.alert.retry.max_attempts";

    private static final String DOMIBUS_ALERT_SUPER_RETRY_MAX_ATTEMPTS = "domibus.alert.super.retry.max_attempts";

    static final String DOMIBUS_ALERT_RETRY_TIME = "domibus.alert.retry.time";

    private static final String DOMIBUS_ALERT_SUPER_RETRY_TIME = "domibus.alert.super.retry.time";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private ConfigurationLoader<MessagingModuleConfiguration> messagingConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> accountDisabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> loginFailureConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ImminentExpirationCertificateModuleConfiguration> imminentExpirationCertificateConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ExpiredCertificateModuleConfiguration> expiredCertificateConfigurationLoader;

    @Autowired
    private ConfigurationLoader<CommonConfiguration> commonConfigurationConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AlertEventModuleConfiguration> expiredPasswordConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AlertEventModuleConfiguration> imminentPasswordExpirationConfigurationLoader;

    @Autowired
    private DomainContextProvider domainContextProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public MessagingModuleConfiguration getMessageCommunicationConfiguration() {
        return messagingConfigurationLoader.getConfiguration(this::readMessageConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return accountDisabledConfigurationLoader.getConfiguration(this::readAccountDisabledConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginFailureModuleConfiguration getLoginFailureConfiguration() {
        return loginFailureConfigurationLoader.getConfiguration(this::readLoginFailureConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImminentExpirationCertificateModuleConfiguration getImminentExpirationCertificateConfiguration() {
        return imminentExpirationCertificateConfigurationLoader.getConfiguration(this::readImminentExpirationCertificateConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpiredCertificateModuleConfiguration getExpiredCertificateConfiguration() {
        return expiredCertificateConfigurationLoader.getConfiguration(this::readExpiredCertificateConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommonConfiguration getCommonConfiguration() {
        return commonConfigurationConfigurationLoader.getConfiguration(this::readCommonConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        switch (alert.getAlertType()) {
            case MSG_STATUS_CHANGED:
                return getMessageCommunicationConfiguration().getAlertLevel(alert);
            case USER_ACCOUNT_DISABLED:
                return getAccountDisabledConfiguration().getAlertLevel(alert);
            case USER_LOGIN_FAILURE:
                return getLoginFailureConfiguration().getAlertLevel(alert);
            case CERT_IMMINENT_EXPIRATION:
                return getImminentExpirationCertificateConfiguration().getAlertLevel(alert);
            case CERT_EXPIRED:
                return getExpiredCertificateConfiguration().getAlertLevel(alert);
            case PASSWORD_IMMINENT_EXPIRATION:
            case PASSWORD_EXPIRED:
                return getRepetitiveEventConfiguration(alert.getAlertType()).getAlertLevel(alert);
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
            case MSG_STATUS_CHANGED:
                return getMessageCommunicationConfiguration().getMailSubject();
            case USER_ACCOUNT_DISABLED:
                return getAccountDisabledConfiguration().getMailSubject();
            case USER_LOGIN_FAILURE:
                return getLoginFailureConfiguration().getMailSubject();
            case CERT_IMMINENT_EXPIRATION:
                return getImminentExpirationCertificateConfiguration().getMailSubject();
            case CERT_EXPIRED:
                return getExpiredCertificateConfiguration().getMailSubject();
            case PASSWORD_IMMINENT_EXPIRATION:
            case PASSWORD_EXPIRED:
                return getRepetitiveEventConfiguration(alertType).getMailSubject();
            default:
                LOG.error("Invalid alert type[{}]", alertType);
                throw new IllegalArgumentException("Invalid alert type");
        }
    }


    @Override
    public Boolean isAlertModuleEnabled() {
        String propertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_ACTIVE, DOMIBUS_ALERT_SUPER_ACTIVE);
        return Boolean.valueOf(domibusPropertyProvider.getDomainProperty(DomainService.DEFAULT_DOMAIN, propertyName));
    }

    @Override
    public String getSendEmailActivePropertyName() {
        return getDomainOrSuperProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE, DOMIBUS_ALERT_SUPER_MAIL_SENDING_ACTIVE);
    }

    @Override
    public String getAlertRetryMaxAttemptPropertyName() {
        return getDomainOrSuperProperty(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, DOMIBUS_ALERT_SUPER_RETRY_MAX_ATTEMPTS);
    }

    @Override
    public String getAlertRetryTimePropertyName() {
        return getDomainOrSuperProperty(DOMIBUS_ALERT_RETRY_TIME, DOMIBUS_ALERT_SUPER_RETRY_TIME);
    }


    private String getDomainOrSuperProperty(final String domainPropertyName, final String superPropertyName) {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if (currentDomain == null) {
            return superPropertyName;
        }
        return domainPropertyName;
    }

    protected CommonConfiguration readCommonConfiguration(Domain domain) {
        final boolean emailActive = Boolean.parseBoolean(domibusPropertyProvider.getOptionalDomainProperty(getSendEmailActivePropertyName(), "false"));
        final String alertCleanerLifeTimePropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, DOMIBUS_ALERT_SUPER_CLEANER_ALERT_LIFETIME);
        final Integer alertLifeTimeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(alertCleanerLifeTimePropertyName, "20"));

        if (!emailActive) {
            return new CommonConfiguration(alertLifeTimeInDays);
        }

        return readDomainEmailConfiguration(domain, alertLifeTimeInDays);
    }

    private CommonConfiguration readDomainEmailConfiguration(Domain domain, Integer alertLifeTimeInDays) {
        final String alertSenderPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_SENDER_EMAIL, DOMIBUS_ALERT_SUPER_SENDER_EMAIL);
        final String alertEmailSender = domibusPropertyProvider.getProperty(domain, alertSenderPropertyName);
        final String alertReceiverPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_RECEIVER_EMAIL, DOMIBUS_ALERT_SUPER_RECEIVER_EMAIL);
        final String alertEmailReceiver = domibusPropertyProvider.getProperty(domain, alertReceiverPropertyName);

        boolean missConfigured = false;
        if (StringUtils.isEmpty(alertEmailReceiver) || StringUtils.isEmpty(alertEmailSender)) {
            missConfigured = true;
        } else {
            try {
                InternetAddress receiverAddress = new InternetAddress(alertEmailReceiver);
                InternetAddress senderAddress = new InternetAddress(alertEmailSender);
                receiverAddress.validate();
                senderAddress.validate();
            } catch (AddressException ae) {
                missConfigured = true;
            }
        }
        if (missConfigured) {
            LOG.error("Alert module can not send email, mail sender property name:[{}]/value[{}] and receiver property name:[{}]/value[{}] are mandatory in domain:[{}]",alertSenderPropertyName, alertEmailSender, alertReceiverPropertyName,alertEmailReceiver, domain);
            throw new IllegalArgumentException("Invalid email address configured for the alert module.");
        }
        return new CommonConfiguration(alertLifeTimeInDays, alertEmailSender, alertEmailReceiver);
    }

    protected MessagingModuleConfiguration readMessageConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean messageAlertActive = Boolean.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !messageAlertActive) {
                LOG.debug("domain:[{}] Alert message status change module is inactive for the following reason:global alert module active[{}], message status change module active[{}]", domain, alertActive, messageAlertActive);
                return new MessagingModuleConfiguration();
            }
            final String messageCommunicationStates = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            final String messageCommunicationLevels = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL, LOW);
            final String mailSubject = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, MESSAGE_STATUS_CHANGE_MAIL_SUBJECT);

            if (StringUtils.isEmpty(messageCommunicationStates) || StringUtils.isEmpty(messageCommunicationLevels)) {
                LOG.warn("Message status change alert module misconfiguration -> states[{}], levels[{}]", messageCommunicationStates, messageCommunicationLevels);
                return new MessagingModuleConfiguration();
            }
            final String[] states = messageCommunicationStates.split(",");
            final String[] levels = messageCommunicationLevels.split(",");
            final boolean eachStatusHasALevel = (states.length == levels.length);
            LOG.debug("Each message status has his own level[{}]", eachStatusHasALevel);

            MessagingModuleConfiguration messagingConfiguration = new MessagingModuleConfiguration(mailSubject);
            IntStream.
                    range(0, states.length).
                    mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(MessageStatus.valueOf(states[i]), AlertLevel.valueOf(levels[eachStatusHasALevel ? i : 0]))).
                    forEach(entry -> messagingConfiguration.addStatusLevelAssociation(entry.getKey(), entry.getValue())); //NOSONAR
            LOG.info("Alert message status change module activated for domain:[{}]", domain);
            return messagingConfiguration;
        } catch (Exception ex) {
            LOG.warn("Error while configuring message communication alerts for domain:[{}], message alert module will be discarded.", domain, ex);
            return new MessagingModuleConfiguration();
        }

    }

    protected AccountDisabledModuleConfiguration readAccountDisabledConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final String accountDisabledAlertPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE, DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_ACTIVE);
            final Boolean accountDisabledActive = Boolean.valueOf(domibusPropertyProvider.getDomainProperty(domain, accountDisabledAlertPropertyName, Boolean.FALSE.toString()));
            if (!alertActive || !accountDisabledActive) {
                LOG.debug("domain:[{}] Alert account disabled module is inactive for the following reason:global alert module active[{}], account disabled module active[{}]", domain, alertActive, accountDisabledActive);
                return new AccountDisabledModuleConfiguration();
            }

            final String accountDisabledLevelPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL, DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_LEVEL);
            final AlertLevel accountDisabledAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, accountDisabledLevelPropertyName, LOW));
            final String accountDisabledMomentPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT, DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_MOMENT);
            final AccountDisabledMoment accountDisabledMoment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getDomainProperty(domain, accountDisabledMomentPropertyName, WHEN_BLOCKED));
            final String accountDisabledSubjectPropertyName=getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT,DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_SUBJECT);
            final String accountDisabledMailSubject = domibusPropertyProvider.getDomainProperty(domain, accountDisabledSubjectPropertyName, ACCOUNT_DISABLED_MAIL_SUBJECT);

            LOG.info("Alert account disabled module activated for domain:[{}]", domain);
            return new AccountDisabledModuleConfiguration(
                    accountDisabledAlertLevel,
                    accountDisabledMoment,
                    accountDisabledMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading authenticator alert module configuration for domain:[{}], ", domain, e);
            return new AccountDisabledModuleConfiguration();
        }

    }

    protected LoginFailureModuleConfiguration readLoginFailureConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();

            final String alertActivePropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE, DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_ACTIVE);
            final Boolean loginFailureActive = Boolean.valueOf(domibusPropertyProvider.getDomainProperty(domain, alertActivePropertyName, Boolean.FALSE.toString()));

            if (!alertActive || !loginFailureActive) {
                LOG.debug("Alert Login failure module is inactive for the following reason:global alert module active[{}], login failure module active[{}]", domain, alertActive, loginFailureActive);
                return new LoginFailureModuleConfiguration();
            }
            final String alertLevelPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL, DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_LEVEL);
            final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, alertLevelPropertyName, LOW));

            final String alertEmailSubjectPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_MAIL_SUBJECT);
            final String loginFailureMailSubject = domibusPropertyProvider.getDomainProperty(domain, alertEmailSubjectPropertyName, LOGIN_FAILURE_MAIL_SUBJECT);

            LOG.info("Alert login failure module activated for domain:[{}]", domain);
            return new LoginFailureModuleConfiguration(
                    loginFailureAlertLevel,
                    loginFailureMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading authenticator alert module configuration for domain:[{}], ", domain, e);
            return new LoginFailureModuleConfiguration();
        }

    }

    protected ImminentExpirationCertificateModuleConfiguration readImminentExpirationCertificateConfiguration(Domain domain) {

        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean imminentExpirationActive = Boolean.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !imminentExpirationActive) {
                LOG.debug("domain:[{}] Alert certificate imminent expiration module is inactive for the following reason:global alert module active[{}], certificate imminent expiration module active[{}]", domain, alertActive, imminentExpirationActive);
                return new ImminentExpirationCertificateModuleConfiguration();
            }
            final Integer imminentExpirationDelay = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS, "61"));
            final Integer imminentExpirationFrequency = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS, "14"));
            final AlertLevel imminentExpirationAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL, LOW));
            final String imminentExpirationMailSubject = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT, CERTIFICATE_IMMINENT_EXPIRATION_MAIL_SUBJECT);

            LOG.info("Alert certificate imminent expiration module activated for domain:[{}]", domain);
            return new ImminentExpirationCertificateModuleConfiguration(
                    imminentExpirationDelay,
                    imminentExpirationFrequency,
                    imminentExpirationAlertLevel,
                    imminentExpirationMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain, e);
            return new ImminentExpirationCertificateModuleConfiguration();
        }

    }

    protected ExpiredCertificateModuleConfiguration readExpiredCertificateConfiguration(Domain domain) {

        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean expiredActive = Boolean.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !expiredActive) {
                LOG.debug("domain:[{}] Alert certificate expired module is inactive for the following reason:global alert module active[{}], certificate expired module active[{}]", domain, alertActive, expiredActive);
                return new ExpiredCertificateModuleConfiguration();
            }
            final Integer revokedFrequency = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS, "7"));
            final Integer revokedDuration = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS, "92"));
            final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_LEVEL, LOW));
            final String expiredMailSubject = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT, CERTIFICATE_EXPIRED_MAIL_SUBJECT);

            LOG.info("Alert certificate expired activated for domain:[{}]", domain);
            return new ExpiredCertificateModuleConfiguration(
                    revokedFrequency,
                    revokedDuration,
                    revocationLevel,
                    expiredMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain, e);
            return new ExpiredCertificateModuleConfiguration();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertEventModuleConfiguration getRepetitiveEventConfiguration(AlertType alertType) {
        ConfigurationLoader<AlertEventModuleConfiguration> alertEventConfigurationLoader = null;
        if (alertType == AlertType.PASSWORD_IMMINENT_EXPIRATION)
            alertEventConfigurationLoader = imminentPasswordExpirationConfigurationLoader;
        else if (alertType == AlertType.PASSWORD_EXPIRED)
            alertEventConfigurationLoader = expiredPasswordConfigurationLoader;

        return alertEventConfigurationLoader.getConfiguration(new ConfigurationReader(alertType)::readAlertConfiguration);
    }


    class ConfigurationReader {
        AlertType alertType;
        String property, defaultSubject;

        public ConfigurationReader(AlertType alertType) {
            this.alertType = alertType;

            if (alertType.equals(AlertType.PASSWORD_IMMINENT_EXPIRATION)) {
                this.property = "domibus.alert.password.imminent_expiration";
                this.defaultSubject = "Password imminent expiration";
            } else if (alertType.equals(AlertType.PASSWORD_EXPIRED)) {
                this.property = "domibus.alert.password.expired";
                this.defaultSubject = "Password expired";
            }
        }

        public <T> AlertEventModuleConfiguration readAlertConfiguration(Domain domain) {
            try {
                final Boolean alertModuleActive = isAlertModuleEnabled();
                final Boolean eventActive = Boolean.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".active", Boolean.FALSE.toString()));
                if (!alertModuleActive || !eventActive) {
                    LOG.debug("domain:[{}] Alert {} module is inactive for the following reason: global alert module active[{}], {} module active[{}]", domain, defaultSubject, alertModuleActive, eventActive);
                    return new AlertEventModuleConfiguration(alertType);
                }

                final Integer delay = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".delay_days", "61"));
                final Integer frequency = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".frequency_days", "14"));
                final AlertLevel alertLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".level", LOW));
                final String mailSubject = domibusPropertyProvider.getDomainProperty(domain, property + ".mail.subject", defaultSubject);

                LOG.info("Alert {} module activated for domain:[{}]", defaultSubject, domain);
                return new AlertEventModuleConfiguration(alertType, delay, frequency, alertLevel, mailSubject);

            } catch (Exception e) {
                LOG.warn("An error occurred while reading {} alert module configuration for domain:[{}], ", defaultSubject, domain, e);
                return new AlertEventModuleConfiguration(alertType);
            }

        }

    }

}
