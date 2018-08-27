package eu.domibus.core.alerts;

import com.google.common.collect.Sets;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import freemarker.template.Configuration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import static eu.domibus.core.alerts.MailSender.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class MailSenderTest {

    @Tested
    private MailSender mailSender;

    @Injectable
    private Configuration freemarkerConfig;

    @Injectable
    private JavaMailSenderImpl javaMailSender;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomainContextProvider domainProvider;

    @Injectable
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Test
    public void initMailSender(@Mocked final Properties javaMailProperties,@Mocked final Predicate predicate) {
        final String smtpUrl = "smtpUrl";
        final String port = "25";
        final String user = "user";
        final String password = "password";
        final String dynamicPropertyName = "domibus.alert.mail.smtp.port";
        final String dynamicSmtpPort = "450";
        final Set<String> dynamicPropertySet= Sets.newHashSet(dynamicPropertyName);
        new Expectations(){{
            multiDomainAlertConfigurationService.isAlertModuleEnabled();
            result=true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_URL);
            result= smtpUrl;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT);
            result = port;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_USER);
            result= user;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD);
            result= password;
            domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE);
            result= true;
            javaMailSender.getJavaMailProperties();
            result=javaMailProperties;
            domibusPropertyProvider.filterPropertiesName(withAny(predicate));
            result=dynamicPropertySet;
            domibusPropertyProvider.getProperty(dynamicPropertyName);
            result = dynamicSmtpPort;
        }};
        mailSender.initMailSender();
        new VerificationsInOrder(){{
            javaMailSender.setHost(smtpUrl);times=1;
            javaMailSender.setPort(25);times=1;
            javaMailSender.setUsername(user);times=1;
            javaMailSender.setPassword(password);times=1;
            javaMailProperties.put("mail.smtp.port",dynamicSmtpPort);
        }};
    }
}