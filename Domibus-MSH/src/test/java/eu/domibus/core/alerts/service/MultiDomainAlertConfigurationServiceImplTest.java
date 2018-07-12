package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;
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
public class MultiDomainAlertConfigurationServiceImplTest {

    @Tested
    private MultiDomainAlertConfigurationServiceImpl multiDomainAlertConfigurationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private ConfigurationLoader<MessagingConfiguration> messagingConfigurationLoader;

    @Injectable
    private ConfigurationLoader<AccountDisabledConfiguration> accountDisabledConfigurationLoader;

    @Injectable
    private ConfigurationLoader<LoginFailureConfiguration> loginFailureConfigurationLoader;

    @Injectable
    private ConfigurationLoader<ImminentExpirationCertificateConfiguration> imminentExpirationCertificateConfigurationLoader;

    @Injectable
    private ConfigurationLoader<ExpiredCertificateConfiguration> expiredCertificateConfigurationLoader;


    @Test
    public void getAlertLevelForMessage(final @Mocked MessagingConfiguration messagingConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.MSG_COMMUNICATION_FAILURE);
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getMessageCommunicationConfiguration();
            result = messagingConfiguration;
        }};
        multiDomainAlertConfigurationService.getAlertLevel(alert);
        new Verifications() {{
            messagingConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelForAccountDisabled(final @Mocked AccountDisabledConfiguration accountDisabledConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_DISABLED);
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getAccountDisabledConfiguration();
            this.result = accountDisabledConfiguration;
        }};
        multiDomainAlertConfigurationService.getAlertLevel(alert);
        new Verifications() {{
            accountDisabledConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }


    @Test
    public void getAlertLevelForLoginFailure(final @Mocked LoginFailureConfiguration loginFailureConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_LOGIN_FAILURE);
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getLoginFailureConfiguration();
            this.result = loginFailureConfiguration;
        }};
        multiDomainAlertConfigurationService.getAlertLevel(alert);
        new Verifications() {{
            loginFailureConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelCertificateForImminentExpiration(final @Mocked ImminentExpirationCertificateConfiguration imminentExpirationCertificateConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getImminentExpirationCertificateConfiguration();
            this.result = imminentExpirationCertificateConfiguration;
        }};
        multiDomainAlertConfigurationService.getAlertLevel(alert);
        new Verifications() {{
            imminentExpirationCertificateConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelForCertificateExpired(final @Mocked ExpiredCertificateConfiguration expiredCertificateConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_EXPIRED);
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getExpiredCertificateConfiguration();
            this.result = expiredCertificateConfiguration;
        }};
        multiDomainAlertConfigurationService.getAlertLevel(alert);
        new Verifications() {{
            expiredCertificateConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForMessage(final @Mocked MessagingConfiguration messagingConfiguration) {
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getMessageCommunicationConfiguration();
            result = messagingConfiguration;
        }};
        multiDomainAlertConfigurationService.getMailSubject(AlertType.MSG_COMMUNICATION_FAILURE);
        new Verifications() {{
            messagingConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForAccountDisabled(final @Mocked AccountDisabledConfiguration accountDisabledConfiguration) {
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getAccountDisabledConfiguration();
            this.result = accountDisabledConfiguration;
        }};
        multiDomainAlertConfigurationService.getMailSubject(AlertType.USER_ACCOUNT_DISABLED);
        new Verifications() {{
            accountDisabledConfiguration.getMailSubject();
            times = 1;
        }};
    }


    @Test
    public void getMailSubjectForLoginFailure(final @Mocked LoginFailureConfiguration loginFailureConfiguration) {
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getLoginFailureConfiguration();
            this.result = loginFailureConfiguration;
        }};
        multiDomainAlertConfigurationService.getMailSubject(AlertType.USER_LOGIN_FAILURE);
        new Verifications() {{
            loginFailureConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForCertificateImminentExpiration(final @Mocked ImminentExpirationCertificateConfiguration imminentExpirationCertificateConfiguration) {
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getImminentExpirationCertificateConfiguration();
            this.result = imminentExpirationCertificateConfiguration;
        }};
        multiDomainAlertConfigurationService.getMailSubject(AlertType.CERT_IMMINENT_EXPIRATION);
        new Verifications() {{
            imminentExpirationCertificateConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForCertificateExpired(final @Mocked ExpiredCertificateConfiguration expiredCertificateConfiguration) {
        new Expectations(multiDomainAlertConfigurationService) {{
            multiDomainAlertConfigurationService.getExpiredCertificateConfiguration();
            this.result = expiredCertificateConfiguration;
        }};
        multiDomainAlertConfigurationService.getMailSubject(AlertType.CERT_EXPIRED);
        new Verifications() {{
            expiredCertificateConfiguration.getMailSubject();
            times = 1;
        }};
    }


    @Test
    public void getAlertLifeTimeInDays() {
        final int alertCleaner = 10;
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);
            this.result = alertCleaner;
        }};
        assertEquals(alertCleaner, multiDomainAlertConfigurationService.getAlertLifeTimeInDays(), 0);
    }

    @Test
    public void getSendFrom() {
        final String sender = "f.f@f.com";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
            this.result = sender;
        }};
        assertEquals(sender, multiDomainAlertConfigurationService.getSendFrom());
    }

    @Test
    public void getSendTo() {
        final String receiver = "f.f@f.com";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);
            this.result = receiver;
        }};
        assertEquals(receiver, multiDomainAlertConfigurationService.getSendTo());
    }

    @Test
    public void isAlertModuleEnabled() {
        final boolean enable = true;
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = enable;
        }};
        assertEquals(enable, multiDomainAlertConfigurationService.isAlertModuleEnabled());
    }

    @Test
    public void readMessageConfigurationEachMessagetStatusItsOwnAlertLevel() {
        Domain domain = new Domain();
        final String mailSubject = "Messsage status changed";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH,LOW";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final MessagingConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
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
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final MessagingConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
        assertTrue(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationIncorrectProperty() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FLOP";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH";
        }};
        final MessagingConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationActiveFalse() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = "false";
        }};
        final MessagingConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationEmptyStatus() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "";
        }};
        final MessagingConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readAccountDisabledConfigurationMainAlertModuleDisabled() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "false";
        }};
        final AccountDisabledConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.readAccountDisabledConfiguration(domain);
        assertFalse(accountDisabledConfiguration.isActive());

    }

    @Test
    public void readAccountDisabledConfiguration() {
        Domain domain = new Domain();
        final String mailSubject = "Accout disabled";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL);
            result="HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT);
            result="AT_LOGON";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT);
            this.result = mailSubject;
        }};
        final AccountDisabledConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.readAccountDisabledConfiguration(domain);
        assertTrue(accountDisabledConfiguration.isActive());
        assertEquals(mailSubject,accountDisabledConfiguration.getMailSubject());
        Alert alert=new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_DISABLED);
        assertEquals(AlertLevel.HIGH,accountDisabledConfiguration.getAlertLevel(alert));
        assertTrue(accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin());

    }

    @Test
    public void readAccountDisabledConfigurationMissconfigured() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL);
            result="HIGHPP";
        }};
        final AccountDisabledConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.readAccountDisabledConfiguration(domain);
        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationMainModuleInactive(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="false";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
            result="true";
        }
        };
        final LoginFailureConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }
    @Test
    public void readLoginFailureConfigurationModuleInactive(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
            result="false";
        }
        };
        final LoginFailureConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }


    @Test
    public void readLoginFailureConfiguration(){
        Domain domain = new Domain();
        final String mailSubject = "Login failure";
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
            result="MEDIUM";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }
        };
        final LoginFailureConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
        assertTrue(loginFailureConfiguration.isActive());
        Alert alert=new Alert();
        alert.setAlertType(AlertType.USER_LOGIN_FAILURE);
        assertEquals(AlertLevel.MEDIUM,loginFailureConfiguration.getAlertLevel(alert));
        assertEquals(mailSubject,loginFailureConfiguration.getMailSubject());
    }

    @Test
    public void readLoginFailureConfigurationWrongAlertLevelConfig(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
            result="WHAT?";
        }
        };
        final LoginFailureConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readImminentExpirationCertificateConfigurationMainModuleDisabled(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="false";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result="true";
        }};
        final ImminentExpirationCertificateConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());

    }
    @Test
    public void readImminentExpirationCertificateConfigurationModuleDisabled(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result="false";
        }};
        final ImminentExpirationCertificateConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());

    }

    @Test
    public void readImminentExpirationCertificateConfigurationModule(){
        Domain domain = new Domain();
        final String mailSubject = "Certificate imminent expiration";
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
            result="60";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS);
            result="10";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL);
            result="MEDIUM";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final ImminentExpirationCertificateConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertTrue(imminentExpirationCertificateConfiguration.isActive());
        assertEquals(mailSubject,imminentExpirationCertificateConfiguration.getMailSubject());
        assertEquals(60,imminentExpirationCertificateConfiguration.getImminentExpirationDelay(),0);
        assertEquals(10,imminentExpirationCertificateConfiguration.getImminentExpirationFrequency(),0);
        Alert alert=new Alert();
        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
        assertEquals(AlertLevel.MEDIUM,imminentExpirationCertificateConfiguration.getAlertLevel(alert));

    }

    @Test
    public void readImminentExpirationCertificateConfigurationModuleWrongConfig(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
            result="Wrong should be integer";
        }};
        final ImminentExpirationCertificateConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationMainModuleInactive(){
            new Expectations(){{
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
                result="false";
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
                result="true";
            }};
        final ExpiredCertificateConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationModuleInactive(){
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result="false";
        }};
        final ExpiredCertificateConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfiguration(){
        final String mailSubject = "Certificate expired";
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
            result="20";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
            result="10";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL);
            result="LOW";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);
            this.result = mailSubject;


        }};
        final ExpiredCertificateConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
        assertTrue(expiredCertificateConfiguration.isActive());
        assertEquals(20,expiredCertificateConfiguration.getExpiredFrequency(),0);
        assertEquals(10,expiredCertificateConfiguration.getExpiredDuration(),0);
        Alert alert=new Alert();
        alert.setAlertType(AlertType.CERT_EXPIRED);
        assertEquals(AlertLevel.LOW,expiredCertificateConfiguration.getAlertLevel(alert));
        assertEquals(mailSubject,expiredCertificateConfiguration.getMailSubject());
    }

    @Test
    public void readExpiredCertificateConfigurationIncorrect(){
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result="true";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
            result="WRONG";


        }};
        final ExpiredCertificateConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }



}