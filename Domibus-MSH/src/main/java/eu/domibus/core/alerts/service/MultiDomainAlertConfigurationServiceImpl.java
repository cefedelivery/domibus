package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MultiDomainAlertConfigurationServiceImpl implements MultiDomainAlertConfigurationService {

    private final static Logger LOG = LoggerFactory.getLogger(MultiDomainAlertConfigurationServiceImpl.class);

    private static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE = "domibus.alert.msg.communication_failure.active";

    private static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES = "domibus.alert.msg.communication_failure.states";

    private static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL = "domibus.alert.msg.communication_failure.level";
    public static final String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT = "domibus.alert.msg.communication_failure.mail.subject";

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

    @Override
    public AccountDisabledConfiguration getAccountDisabledConfiguration() {
        return accountDisabledConfigurationLoader.getConfiguration(this::readAuthenticatorConfiguration);
    }

    @Override
    public LoginFailureConfiguration getLoginFailureConfigurationLoader() {
        return loginFailureConfigurationLoader.getConfiguration(this::readLoginFailureConfiguration);
    }

    @Override
    public ImminentExpirationCertificateConfiguration getImminentExpirationCertificateConfiguration() {
        return imminentExpirationCertificateConfigurationLoader.getConfiguration(this::readImminentExpirationCertificateConfiguration);
    }

    @Override
    public ExpiredCertificateConfiguration getExpiredCertificateConfiguration() {
        return expiredCertificateConfigurationLoader.getConfiguration(this::readExpiredCertificateConfiguration);
    }

    private MessagingConfiguration readMessageConfiguration(Domain domain) {
        try {
            final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
            if (!alertActive) {
                return new MessagingConfiguration();
            }
            final Boolean messageAlertActif = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE));
            Map<MessageStatus, AlertLevel> messageStatusLevels = new HashMap<>();
            if (messageAlertActif) {
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
            LOG.debug("Message communication module active:[{}]", messageAlertActif);
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
            final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
            if (!alertActive) {
                LOG.debug("Alert module is inactive");
                return new AccountDisabledConfiguration( false);
            }

            final Boolean accountDisabledActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.account_disabled.active"));
            final AlertLevel accountDisabledAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.account_disabled.level"));
            final AccountDisabledMoment accountDisabledMoment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.account_disabled.moment"));
            final String accountDisabledMailSubject = domibusPropertyProvider.getProperty("domibus.alert.user.account_disabled.subject");

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
            final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
            if (!alertActive) {
                LOG.debug("Alert module is inactive");
                return new LoginFailureConfiguration(false);
            }
            final Boolean loginFailureActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.login_failure.active"));
            final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.login_failure.level"));
            final String loginFailureMailSubject = domibusPropertyProvider.getProperty("domibus.alert.user.login_failure.mail.subject");

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
        final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
        if (!alertActive) {
            LOG.debug("Alert module is inactive");
            return new ImminentExpirationCertificateConfiguration(false);
        }
        try {
            final Boolean imminentExpirationActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.active"));
            final Integer imminentExpirationDelay = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.delay_days"));
            final Integer imminentExpirationFrequency = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.frequency_days"));
            final AlertLevel imminentExpirationAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.level"));
            final String imminentExpirationMailSubject = domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.mail.subject");

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
        final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
        if (!alertActive) {
            LOG.debug("Alert module is inactive");
            return new ExpiredCertificateConfiguration(false);
        }
        try {
            final Boolean revocatedActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.active"));
            final Integer revocatedFrequency = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.frequency_days"));
            final Integer revocatedDuration = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.duration_days"));
            final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.level"));
            final String expiredMailSubject = domibusPropertyProvider.getProperty("domibus.alert.cert.expired.mail.subject");

            return new ExpiredCertificateConfiguration(
                    revocatedActive,
                    revocatedFrequency,
                    revocatedDuration,
                    revocationLevel,
                    expiredMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain);
            return new ExpiredCertificateConfiguration(false);
        }


    }


}
