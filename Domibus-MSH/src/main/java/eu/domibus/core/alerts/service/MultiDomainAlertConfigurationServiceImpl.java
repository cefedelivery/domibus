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

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private ConfigurationLoader<MessagingConfiguration> messagingConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AuthenticatorConfiguration> authenticatorConfigurationLoader;

    @Autowired
    private ConfigurationLoader<CertificateScannerConfiguration> certificateScannerConfigurationLoader;

    /**
     * {@inheritDoc}
     */
    @Override
    public MessagingConfiguration getMessageCommunicationConfiguration() {
        return messagingConfigurationLoader.getConfiguration(this::readMessageConfiguration);
    }

    public AuthenticatorConfiguration getAuthenticatorConfiguration() {
        return authenticatorConfigurationLoader.getConfiguration(this::readAuthenticatorConfiguration);
    }

    public CertificateScannerConfiguration getCertificateScannerConfiguration() {
        return certificateScannerConfigurationLoader.getConfiguration(this::readCertificateScannerConfiguration);
    }

    private MessagingConfiguration readMessageConfiguration(Domain domain) {
        try {
            final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
            if (!alertActive) {
                return new MessagingConfiguration(false);
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
            MessagingConfiguration messagingConfiguration = new MessagingConfiguration(messageAlertActif);
            messageStatusLevels.forEach((messageStatus, alertLevel) -> {
                LOG.debug("Watched message status:[{}] with level[{}]", messageStatus, alertLevel);
                messagingConfiguration.addStatusLevelAssociation(messageStatus, alertLevel);

            });
            return messagingConfiguration;
        } catch (Exception ex) {
            LOG.error("Error while configuring message communication alerts for domain:[{}], message alert module will be discarded.", domain, ex);
            return new MessagingConfiguration(false);
        }

    }

    private AuthenticatorConfiguration readAuthenticatorConfiguration(Domain domain) {
        try {
            final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
            if (!alertActive) {
                LOG.debug("Alert module is inactive");
                return new AuthenticatorConfiguration(false, false);
            }
            final Boolean loginFailureActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.login_failure.active"));
            final Boolean accountDisabledActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.account_disabled.active"));
            final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.login_failure.level"));
            final AlertLevel accountDisabledAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.account_disabled.level"));
            final AccountDisabledMoment accountDisabledMoment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty("domibus.alert.user.account_disabled.moment"));
            return new AuthenticatorConfiguration(loginFailureActive, accountDisabledActive, loginFailureAlertLevel, accountDisabledAlertLevel, accountDisabledMoment);

        } catch (Exception e) {
            LOG.error("An error occurred while reading authenticator alert module configuration for domain:[{}], ", domain);
            return new AuthenticatorConfiguration(false, false);
        }

    }

    private CertificateScannerConfiguration readCertificateScannerConfiguration(Domain domain) {
        final Boolean alertActive = Boolean.valueOf(domibusPropertyProvider.getProperty(Alert.DOMIBUS_ALERT_ACTIVE));
        if (!alertActive) {
            LOG.debug("Alert module is inactive");
            return new CertificateScannerConfiguration(false, false);
        }
        try {
        final Boolean imminentExpirationActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.active"));
        final Integer imminentExpirationDelay = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.delay_days"));
        final Integer imminentExpirationFrequency = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.frequency_days"));
        final AlertLevel imminentExpirationAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.imminent_expiration.level"));

        final Boolean revocatedActive = Boolean.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.active"));
        final Integer revocatedFrequency = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.frequency_days"));
        final Integer revocatedDuration = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.duration_days"));
        final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty("domibus.alert.cert.expired.level"));

        return new CertificateScannerConfiguration(
                imminentExpirationActive,
                imminentExpirationDelay,
                imminentExpirationFrequency,
                imminentExpirationAlertLevel,
                revocatedActive,
                revocatedFrequency,
                revocatedDuration,
                revocationLevel);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain);
            return new CertificateScannerConfiguration(false, false);
        }



    }


}
