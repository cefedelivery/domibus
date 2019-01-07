package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.core.alerts.service.MultiDomainAlertConfigurationServiceImpl.*;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class MultiDomainAlertModuleConfigurationServiceImplTest {

    @Tested
    private MultiDomainAlertConfigurationServiceImpl configurationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private ConfigurationLoader<MessagingModuleConfiguration> messagingConfigurationLoader;

    @Injectable
    private ConfigurationLoader<AccountDisabledModuleConfiguration> accountDisabledConfigurationLoader;

    @Injectable
    private ConfigurationLoader<LoginFailureModuleConfiguration> loginFailureConfigurationLoader;

    @Injectable
    private ConfigurationLoader<ImminentExpirationCertificateModuleConfiguration> imminentExpirationCertificateConfigurationLoader;

    @Injectable
    private ConfigurationLoader<ExpiredCertificateModuleConfiguration> expiredCertificateConfigurationLoader;

    @Injectable
    private ConfigurationLoader<CommonConfiguration> commonConfigurationConfigurationLoader;

    @Injectable
    private ConfigurationLoader<RepetitiveAlertModuleConfiguration> expiredPasswordConfigurationLoader;

    @Injectable
    private ConfigurationLoader<RepetitiveAlertModuleConfiguration> imminentPasswordExpirationConfigurationLoader;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    RepetitiveAlertConfigurationHolder alertConfigurationHolder;

    @Injectable
    ConfigurationLoader<LoginFailureModuleConfiguration> pluginLoginFailureConfigurationLoader;

    @Injectable
    ConfigurationLoader<AccountDisabledModuleConfiguration> pluginAccountDisabledConfigurationLoader;

    @Test
    public void getAlertLevelForMessage(final @Mocked MessagingModuleConfiguration messagingConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.MSG_STATUS_CHANGED);
        new Expectations(configurationService) {{
            configurationService.getMessageCommunicationConfiguration();
            result = messagingConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            messagingConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelForAccountDisabled(final @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_DISABLED);
        new Expectations(configurationService) {{
            configurationService.getAccountDisabledConfiguration();
            this.result = accountDisabledConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            accountDisabledConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }


    @Test
    public void getAlertLevelForLoginFailure(final @Mocked LoginFailureModuleConfiguration loginFailureConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_LOGIN_FAILURE);
        new Expectations(configurationService) {{
            configurationService.getLoginFailureConfiguration();
            this.result = loginFailureConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            loginFailureConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelCertificateForImminentExpiration(final @Mocked ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
        new Expectations(configurationService) {{
            configurationService.getImminentExpirationCertificateConfiguration();
            this.result = imminentExpirationCertificateConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            imminentExpirationCertificateConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelForCertificateExpired(final @Mocked ExpiredCertificateModuleConfiguration expiredCertificateConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_EXPIRED);
        new Expectations(configurationService) {{
            configurationService.getExpiredCertificateConfiguration();
            this.result = expiredCertificateConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            expiredCertificateConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForMessage(final @Mocked MessagingModuleConfiguration messagingConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getMessageCommunicationConfiguration();
            result = messagingConfiguration;
        }};
        configurationService.getMailSubject(AlertType.MSG_STATUS_CHANGED);
        new Verifications() {{
            messagingConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForAccountDisabled(final @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getAccountDisabledConfiguration();
            this.result = accountDisabledConfiguration;
        }};
        configurationService.getMailSubject(AlertType.USER_ACCOUNT_DISABLED);
        new Verifications() {{
            accountDisabledConfiguration.getMailSubject();
            times = 1;
        }};
    }


    @Test
    public void getMailSubjectForLoginFailure(final @Mocked LoginFailureModuleConfiguration loginFailureConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getLoginFailureConfiguration();
            this.result = loginFailureConfiguration;
        }};
        configurationService.getMailSubject(AlertType.USER_LOGIN_FAILURE);
        new Verifications() {{
            loginFailureConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForCertificateImminentExpiration(final @Mocked ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getImminentExpirationCertificateConfiguration();
            this.result = imminentExpirationCertificateConfiguration;
        }};
        configurationService.getMailSubject(AlertType.CERT_IMMINENT_EXPIRATION);
        new Verifications() {{
            imminentExpirationCertificateConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForCertificateExpired(final @Mocked ExpiredCertificateModuleConfiguration expiredCertificateConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getExpiredCertificateConfiguration();
            this.result = expiredCertificateConfiguration;
        }};
        configurationService.getMailSubject(AlertType.CERT_EXPIRED);
        new Verifications() {{
            expiredCertificateConfiguration.getMailSubject();
            times = 1;
        }};
    }


    @Test
    public void readCommonConfiguration(@Mocked final Domain domain) {
        final String sender = "thomas.dussart@ec.eur.europa.com";
        final String receiver = "f.f@f.com";
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_SENDER_EMAIL);
            result = sender;
            domibusPropertyProvider.getProperty(domain, DOMIBUS_ALERT_RECEIVER_EMAIL);
            result = receiver;
            domibusPropertyProvider.getBooleanOptionalDomainProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE);
            result = true;
            domibusPropertyProvider.getIntegerOptionalDomainProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);
            result = 20;
        }};
        final CommonConfiguration commonConfiguration = configurationService.readCommonConfiguration(domain);
        assertEquals(sender, commonConfiguration.getSendFrom());
        assertEquals(receiver, commonConfiguration.getSendTo());
        assertEquals(20, commonConfiguration.getAlertLifeTimeInDays(), 0);
    }


    @Test
    public void isAlertModuleEnabled() {
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            this.result = true;
        }};
        assertEquals(true, configurationService.isAlertModuleEnabled());
    }

    @Test
    public void readMessageConfigurationEachMessagetStatusItsOwnAlertLevel() {
        Domain domain = new Domain();
        final String mailSubject = "Messsage status changed";
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH,LOW";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration(domain);
        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
        assertEquals(AlertLevel.LOW, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
        assertTrue(messagingConfiguration.isActive());
    }

    @Test
    public void readMessageConfigurationEachMessagetStatusHasTheSameAlertLevel() {
        Domain domain = new Domain();
        final String mailSubject = "Messsage status changed";
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration(domain);
        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
        assertTrue(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationIncorrectProperty() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FLOP";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH";
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationActiveFalse() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            this.result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = false;
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationEmptyStatus() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            this.result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "";
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readAccountDisabledConfigurationMainAlertModuleDisabled() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            this.result = false;
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationService.new ConsoleAccountDisabledConfigurationReader().readConfiguration(domain);
        assertFalse(accountDisabledConfiguration.isActive());

    }

    @Test
    public void readAccountDisabledConfiguration() {
        Domain domain = new Domain();
        final String mailSubject = "Accout disabled";
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT);
            result = "AT_LOGON";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT);
            this.result = mailSubject;
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationService.new ConsoleAccountDisabledConfigurationReader().readConfiguration(domain);
        assertTrue(accountDisabledConfiguration.isActive());
        assertEquals(mailSubject, accountDisabledConfiguration.getMailSubject());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_DISABLED);
        assertEquals(AlertLevel.HIGH, accountDisabledConfiguration.getAlertLevel(alert));
        assertTrue(accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin());

    }

    @Test
    public void readPluginAccountDisabledConfigurationTest() {
        Domain domain = new Domain();
        final String mailSubject = "Plugin accout disabled";
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, "domibus.alert.plugin.user.account_disabled.active");
            result = true;
            domibusPropertyProvider.getDomainProperty(domain, "domibus.alert.plugin.user.account_disabled.level");
            result = "HIGH";
            domibusPropertyProvider.getDomainProperty(domain, "domibus.alert.plugin.user.account_disabled.moment");
            result = "AT_LOGON";
            domibusPropertyProvider.getDomainProperty(domain, "domibus.alert.plugin.user.account_disabled.subject");
            this.result = mailSubject;
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationService.new
                PluginAccountDisabledConfigurationReader().readConfiguration(domain);

        assertTrue(accountDisabledConfiguration.isActive());
        assertEquals(mailSubject, accountDisabledConfiguration.getMailSubject());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.PLUGIN_USER_ACCOUNT_DISABLED);
        assertEquals(AlertLevel.HIGH, accountDisabledConfiguration.getAlertLevel(alert));
        assertTrue(accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin());

    }

    @Test
    public void readAccountDisabledConfigurationMissconfigured() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL);
            result = "HIGHPP";
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationService.new ConsoleAccountDisabledConfigurationReader().readConfiguration(domain);
        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationMainModuleInactive() {
        Domain domain = new Domain();
        new Expectations() {
            {
                domainContextProvider.getCurrentDomainSafely();
                result = DomainService.DEFAULT_DOMAIN;
                domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
                result = false;
                domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationService.new ConsoleLoginFailConfigurationReader().readConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationModuleInactive() {
        Domain domain = new Domain();
        new Expectations() {
            {
                domainContextProvider.getCurrentDomainSafely();
                result = DomainService.DEFAULT_DOMAIN;
                domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = false;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationService.new ConsoleLoginFailConfigurationReader().readConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }


    @Test
    public void readLoginFailureConfiguration() {
        Domain domain = new Domain();
        final String mailSubject = "Login failure";
        new Expectations() {
            {
                domainContextProvider.getCurrentDomainSafely();
                result = DomainService.DEFAULT_DOMAIN;
                domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
                domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
                result = "MEDIUM";
                domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT);
                this.result = mailSubject;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationService.new ConsoleLoginFailConfigurationReader().readConfiguration(domain);
        assertTrue(loginFailureConfiguration.isActive());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_LOGIN_FAILURE);
        assertEquals(AlertLevel.MEDIUM, loginFailureConfiguration.getAlertLevel(alert));
        assertEquals(mailSubject, loginFailureConfiguration.getMailSubject());
    }

    @Test
    public void readLoginFailureConfigurationWrongAlertLevelConfig() {
        Domain domain = new Domain();
        new Expectations() {
            {
                domainContextProvider.getCurrentDomainSafely();
                result = DomainService.DEFAULT_DOMAIN;
                domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
                domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
                result = "WHAT?";
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationService.new ConsoleLoginFailConfigurationReader().readConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readImminentExpirationCertificateConfigurationMainModuleDisabled() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = false;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result = true;
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());

    }

    @Test
    public void readImminentExpirationCertificateConfigurationModuleDisabled(@Mocked final Domain domain) {
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result = false;
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());

    }

    @Test
    public void readImminentExpirationCertificateConfigurationModuleTest() {
        Domain domain = new Domain();
        final String mailSubject = "Certificate imminent expiration";
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result = true;
            domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
            result = 60;
            domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS);
            result = 10;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL);
            result = "MEDIUM";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration(domain);
        assertTrue(imminentExpirationCertificateConfiguration.isActive());
        assertEquals(mailSubject, imminentExpirationCertificateConfiguration.getMailSubject());
        assertEquals(60, imminentExpirationCertificateConfiguration.getImminentExpirationDelay(), 0);
        assertEquals(10, imminentExpirationCertificateConfiguration.getImminentExpirationFrequency(), 0);
        Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
        assertEquals(AlertLevel.MEDIUM, imminentExpirationCertificateConfiguration.getAlertLevel(alert));

    }

    @Test
    public void readExpiredCertificateConfigurationMainModuleInactiveTest() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = false;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result = true;
        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationModuleInactiveTest() {
        Domain domain = new Domain();
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result = false;
        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationTest() {
        Domain domain = new Domain();
        final String mailSubject = "Certificate expired";
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result = true;
            domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
            result = 20;
            domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
            result = 10;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_LEVEL);
            result = "LOW";
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);
            this.result = mailSubject;


        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration(new Domain());
        assertTrue(expiredCertificateConfiguration.isActive());
        assertEquals(20, expiredCertificateConfiguration.getExpiredFrequency(), 0);
        assertEquals(10, expiredCertificateConfiguration.getExpiredDuration(), 0);
        Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_EXPIRED);
        assertEquals(AlertLevel.LOW, expiredCertificateConfiguration.getAlertLevel(alert));
        assertEquals(mailSubject, expiredCertificateConfiguration.getMailSubject());
    }


    @Test
    public void getRepetitiveAlertConfigurationTest() {
        Domain domain = new Domain();
        String property = "domibus.alert.password.expired";
        new Expectations() {
            {
                domainContextProvider.getCurrentDomainSafely();
                result = DomainService.DEFAULT_DOMAIN;

                domibusPropertyProvider.getBooleanDomainProperty(DomainService.DEFAULT_DOMAIN, "domibus.alert.active");
                result = true;

                domibusPropertyProvider.getDomainProperty(domain, property + ".active");
                result = "true";

                domibusPropertyProvider.getDomainProperty(domain, property + ".delay_days");
                result = "15";

                domibusPropertyProvider.getDomainProperty(domain, property + ".frequency_days");
                result = "5";

                domibusPropertyProvider.getDomainProperty(domain, property + ".level");
                result = AlertLevel.MEDIUM.name();

                domibusPropertyProvider.getDomainProperty(domain, property + ".mail.subject");
                result = "my subjects";
            }
        };
        final RepetitiveAlertModuleConfiguration conf = configurationService.new
                RepetitiveAlertConfigurationReader(AlertType.PASSWORD_EXPIRED).readConfiguration(domain);

        assertTrue(conf.isActive());
        assertEquals((long)15, (long)conf.getEventDelay());
        Alert a = new Alert(){{
           setAlertType(AlertType.PASSWORD_EXPIRED);
        }};
        assertEquals(AlertLevel.MEDIUM, conf.getAlertLevel(a));

    }
}