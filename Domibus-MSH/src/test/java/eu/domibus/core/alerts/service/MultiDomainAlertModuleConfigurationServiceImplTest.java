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
public class MultiDomainAlertModuleConfigurationServiceImplTest {

    @Tested
    private MultiDomainAlertConfigurationServiceImpl multiDomainAlertConfigurationService;

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


    @Test
    public void getAlertLevelForMessage(final @Mocked MessagingModuleConfiguration messagingConfiguration) {
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
    public void getAlertLevelForAccountDisabled(final @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration) {
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
    public void getAlertLevelForLoginFailure(final @Mocked LoginFailureModuleConfiguration loginFailureConfiguration) {
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
    public void getAlertLevelCertificateForImminentExpiration(final @Mocked ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration) {
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
    public void getAlertLevelForCertificateExpired(final @Mocked ExpiredCertificateModuleConfiguration expiredCertificateConfiguration) {
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
    public void getMailSubjectForMessage(final @Mocked MessagingModuleConfiguration messagingConfiguration) {
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
    public void getMailSubjectForAccountDisabled(final @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration) {
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
    public void getMailSubjectForLoginFailure(final @Mocked LoginFailureModuleConfiguration loginFailureConfiguration) {
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
    public void getMailSubjectForCertificateImminentExpiration(final @Mocked ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration) {
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
    public void getMailSubjectForCertificateExpired(final @Mocked ExpiredCertificateModuleConfiguration expiredCertificateConfiguration) {
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
    public void readCommonConfiguration(@Mocked final Domain domain) {
        final String sender = "thomas.dussart@ec.eur.europa.com";
        final String receiver = "f.f@f.com";
        new Expectations() {{
            domibusPropertyProvider.getProperty(domain,DOMIBUS_ALERT_SENDER_EMAIL);
            result = sender;
            domibusPropertyProvider.getProperty(domain,DOMIBUS_ALERT_RECEIVER_EMAIL);
            result=receiver;
            domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, "20");
            result="20";
        }};
        final CommonConfiguration commonConfiguration = multiDomainAlertConfigurationService.readCommonConfiguration(domain);
        assertEquals(sender,commonConfiguration.getSendFrom());
        assertEquals(receiver,commonConfiguration.getSendTo());
        assertEquals(20, commonConfiguration.getAlertLifeTimeInDays(),0);
    }


    @Test
    public void isAlertModuleEnabled() {
        new Expectations() {{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = true;
        }};
        assertEquals(true, multiDomainAlertConfigurationService.isAlertModuleEnabled());
    }

    @Test
    public void readMessageConfigurationEachMessagetStatusItsOwnAlertLevel() {
        Domain domain = new Domain();
        final String mailSubject = "Messsage status changed";
        new Expectations() {{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL,LOW);
            result = "HIGH,LOW";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, MESSAGE_STATUS_CHANGE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final MessagingModuleConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
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
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL,LOW);
            result = "HIGH";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, MESSAGE_STATUS_CHANGE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final MessagingModuleConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
        assertTrue(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationIncorrectProperty() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FLOP";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL,LOW);
            result = "HIGH";
        }};
        final MessagingModuleConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationActiveFalse() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result = "false";
        }};
        final MessagingModuleConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readMessageConfigurationEmptyStatus() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL,LOW);
            result = "";
        }};
        final MessagingModuleConfiguration messagingConfiguration = multiDomainAlertConfigurationService.readMessageConfiguration(domain);
        assertFalse(messagingConfiguration.isActive());

    }

    @Test
    public void readAccountDisabledConfigurationMainAlertModuleDisabled() {
        Domain domain = new Domain();
        new Expectations() {{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "false";
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.readAccountDisabledConfiguration(domain);
        assertFalse(accountDisabledConfiguration.isActive());

    }

    @Test
    public void readAccountDisabledConfiguration() {
        Domain domain = new Domain();
        final String mailSubject = "Accout disabled";
        new Expectations() {{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL,LOW);
            result="HIGH";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT,WHEN_BLOCKED);
            result="AT_LOGON";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT,ACCOUNT_DISABLED_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.readAccountDisabledConfiguration(domain);
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
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = "true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL,LOW);
            result="HIGHPP";
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.readAccountDisabledConfiguration(domain);
        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationMainModuleInactive(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="false";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result="true";
        }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }
    @Test
    public void readLoginFailureConfigurationModuleInactive(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result="false";
        }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }


    @Test
    public void readLoginFailureConfiguration(){
        Domain domain = new Domain();
        final String mailSubject = "Login failure";
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL,LOW);
            result="MEDIUM";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, LOGIN_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
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
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL,LOW);
            result="WHAT?";
        }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.readLoginFailureConfiguration(domain);
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readImminentExpirationCertificateConfigurationMainModuleDisabled(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="false";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE,Boolean.FALSE.toString());
            result="true";
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());

    }
    @Test
    public void readImminentExpirationCertificateConfigurationModuleDisabled(@Mocked final Domain domain){
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE,Boolean.FALSE.toString());
            result="false";
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());

    }

    @Test
    public void readImminentExpirationCertificateConfigurationModule(){
        Domain domain = new Domain();
        final String mailSubject = "Certificate imminent expiration";
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS,"61");
            result="60";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS,"14");
            result="10";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL,LOW);
            result="MEDIUM";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT, CERTIFICATE_IMMINENT_EXPIRATION_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertTrue(imminentExpirationCertificateConfiguration.isActive());
        assertEquals(mailSubject,imminentExpirationCertificateConfiguration.getMailSubject());
        assertEquals(60,imminentExpirationCertificateConfiguration.getImminentExpirationDelay(),0);
        assertEquals(10,imminentExpirationCertificateConfiguration.getImminentExpirationFrequency(),0);
        Alert alert=new Alert();
        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
        assertEquals(AlertLevel.MEDIUM,imminentExpirationCertificateConfiguration.getAlertLevel(alert));

    }

    @Test
    public void readImminentExpirationCertificateConfigurationModuleWrongConfig(@Mocked final Domain domain){
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS,"61");
            result="WRONG NUMBER";

        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.readImminentExpirationCertificateConfiguration(domain);
        assertFalse(imminentExpirationCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationMainModuleInactive(){
        Domain domain = new Domain();
            new Expectations(){{
                domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
                result="false";
                domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE,Boolean.FALSE.toString());
                result="true";
            }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationModuleInactive(){
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE,Boolean.FALSE.toString());
            result="false";
        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfiguration(){
        Domain domain = new Domain();
        final String mailSubject = "Certificate expired";
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS,"7");
            result="20";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS,"92");
            result="10";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_LEVEL, LOW);
            result="LOW";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT, CERTIFICATE_EXPIRED_MAIL_SUBJECT);
            this.result = mailSubject;


        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
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
        Domain domain = new Domain();
        new Expectations(){{
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_ACTIVE);
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE,Boolean.FALSE.toString());
            result="true";
            domibusPropertyProvider.getDomainProperty(domain,DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS,"7");
            result="WRONG";


        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.readExpiredCertificateConfiguration(new Domain());
        assertFalse(expiredCertificateConfiguration.isActive());
    }



}