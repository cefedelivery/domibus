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

    private static final Logger LOG = LoggerFactory.getLogger(MultiDomainAlertConfigurationServiceImpl.class);

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

    static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL = "domibus.alert.user.login_failure.level";

    static final String DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE = "domibus.alert.user.login_failure.active";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT = "domibus.alert.user.account_disabled.subject";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT = "domibus.alert.user.account_disabled.moment";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL = "domibus.alert.user.account_disabled.level";

    static final String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE = "domibus.alert.user.account_disabled.active";

    static final String DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME = "domibus.alert.cleaner.alert.lifetime";

    static final String DOMIBUS_ALERT_SENDER_EMAIL = "domibus.alert.sender.email";

    static final String DOMIBUS_ALERT_RECEIVER_EMAIL = "domibus.alert.receiver.email";

    static final String DOMIBUS_ALERT_ACTIVE = "domibus.alert.active";

    static final String MESSAGE_STATUS_CHANGE_MAIL_SUBJECT = "Message status change";

    static final String ACCOUNT_DISABLED_MAIL_SUBJECT = "Account disabled";

    static final String WHEN_BLOCKED = "WHEN_BLOCKED";

    static final String LOW = "LOW";

    static final String LOGIN_FAILURE_MAIL_SUBJECT = "Login failure";

    static final String CERTIFICATE_IMMINENT_EXPIRATION_MAIL_SUBJECT = "Certificate imminent expiration";

    static final String CERTIFICATE_EXPIRED_MAIL_SUBJECT = "Certificate expired";

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
            case MSG_COMMUNICATION_FAILURE:
                return getMessageCommunicationConfiguration().getAlertLevel(alert);
            case USER_ACCOUNT_DISABLED:
                return getAccountDisabledConfiguration().getAlertLevel(alert);
            case USER_LOGIN_FAILURE:
                return getLoginFailureConfiguration().getAlertLevel(alert);
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
                return getLoginFailureConfiguration().getMailSubject();
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
    public Boolean isAlertModuleEnabled() {
        return Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE));
    }

    protected CommonConfiguration readCommonConfiguration(Domain domain) {
        final String alertEmailSender = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_SENDER_EMAIL);
        final String alertEmailReceiver = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_RECEIVER_EMAIL);
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
            LOG.error("Alert module can not send email, mail sender:[{}] and receiver:[{}] are mandatory in domain:[{}]", alertEmailSender, alertEmailReceiver, domain);
            throw new IllegalArgumentException("Invalid email address configured for the alert module.");
        }
        final Integer alertLifeTimeInDays = Integer.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, "20"));
        return new CommonConfiguration(alertLifeTimeInDays, alertEmailSender, alertEmailReceiver);
    }

    protected MessagingModuleConfiguration readMessageConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean messageAlertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !messageAlertActive) {
                LOG.debug("domain:[{}] Alert message status change module is inactive for the following reason:global alert module active[{}], message status change module active[{}]", domain, alertActive, messageAlertActive);
                return new MessagingModuleConfiguration();
            }
            final String messageCommunicationStates = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            final String messageCommunicationLevels = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL, LOW);
            final String mailSubject = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, MESSAGE_STATUS_CHANGE_MAIL_SUBJECT);

            if (StringUtils.isEmpty(messageCommunicationStates) || StringUtils.isEmpty(messageCommunicationLevels)) {
                LOG.warn("Message status change alert module misconfiguration -> states[{}], levels[{}]", messageCommunicationStates, messageCommunicationLevels);
                return new MessagingModuleConfiguration();
            }
            final String[] states = messageCommunicationStates.split(",");
            final String[] levels = messageCommunicationLevels.split(",");
            final boolean eachStatusHasALevel = (states.length == levels.length);
            LOG.debug("Each message status has his own level[{}]",eachStatusHasALevel);

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
            final Boolean accountDisabledActive = Boolean.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !accountDisabledActive) {
                LOG.debug("domain:[{}] Alert account disabled module is inactive for the following reason:global alert module active[{}], account disabled module active[{}]", domain, alertActive, accountDisabledActive);
                return new AccountDisabledModuleConfiguration();
            }

            final AlertLevel accountDisabledAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL, LOW));
            final AccountDisabledMoment accountDisabledMoment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT, WHEN_BLOCKED));
            final String accountDisabledMailSubject = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT, ACCOUNT_DISABLED_MAIL_SUBJECT);

            LOG.info("Alert account disabled module activated for domain:[{}]", domain);
            return new AccountDisabledModuleConfiguration(
                    accountDisabledAlertLevel,
                    accountDisabledMoment,
                    accountDisabledMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading authenticator alert module configuration for domain:[{}], ", domain);
            return new AccountDisabledModuleConfiguration();
        }

    }

    protected LoginFailureModuleConfiguration readLoginFailureConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean loginFailureActive = Boolean.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !loginFailureActive) {
                LOG.debug("domain:[{}] Alert Login failure module is inactive for the following reason:global alert module active[{}], login failure module active[{}]", domain, alertActive, loginFailureActive);
                return new LoginFailureModuleConfiguration();
            }
            final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL, LOW));
            final String loginFailureMailSubject = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, LOGIN_FAILURE_MAIL_SUBJECT);

            LOG.info("Alert login failure module activated for domain:[{}]", domain);
            return new LoginFailureModuleConfiguration(
                    loginFailureAlertLevel,
                    loginFailureMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading authenticator alert module configuration for domain:[{}], ", domain);
            return new LoginFailureModuleConfiguration();
        }

    }

    protected ImminentExpirationCertificateModuleConfiguration readImminentExpirationCertificateConfiguration(Domain domain) {

        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean imminentExpirationActive = Boolean.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !imminentExpirationActive) {
                LOG.debug("domain:[{}] Alert certificate imminent expiration module is inactive for the following reason:global alert module active[{}], certificate imminent expiration module active[{}]", domain, alertActive, imminentExpirationActive);
                return new ImminentExpirationCertificateModuleConfiguration();
            }
            final Integer imminentExpirationDelay = Integer.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS, "61"));
            final Integer imminentExpirationFrequency = Integer.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS, "14"));
            final AlertLevel imminentExpirationAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL, LOW));
            final String imminentExpirationMailSubject = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT, CERTIFICATE_IMMINENT_EXPIRATION_MAIL_SUBJECT);

            LOG.info("Alert certificate imminent expiration module activated for domain:[{}]", domain);
            return new ImminentExpirationCertificateModuleConfiguration(
                    imminentExpirationDelay,
                    imminentExpirationFrequency,
                    imminentExpirationAlertLevel,
                    imminentExpirationMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain);
            return new ImminentExpirationCertificateModuleConfiguration();
        }

    }

    protected ExpiredCertificateModuleConfiguration readExpiredCertificateConfiguration(Domain domain) {

        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean expiredActive = Boolean.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE, Boolean.FALSE.toString()));
            if (!alertActive || !expiredActive) {
                LOG.debug("domain:[{}] Alert certificate expired module is inactive for the following reason:global alert module active[{}], certificate expired module active[{}]", domain, alertActive, expiredActive);
                return new ExpiredCertificateModuleConfiguration();
            }
            final Integer revokedFrequency = Integer.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS, "7"));
            final Integer revokedDuration = Integer.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS, "92"));
            final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_LEVEL, LOW));
            final String expiredMailSubject = domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT, CERTIFICATE_EXPIRED_MAIL_SUBJECT);

            LOG.info("Alert certificate expired activated for domain:[{}]", domain);
            return new ExpiredCertificateModuleConfiguration(
                    revokedFrequency,
                    revokedDuration,
                    revocationLevel,
                    expiredMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain);
            return new ExpiredCertificateModuleConfiguration();
        }
    }

}
