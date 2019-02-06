package eu.domibus.core.alerts;

import com.google.common.collect.Sets;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import freemarker.template.Configuration;
import freemarker.template.TemplateNotFoundException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
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

    final String smtpUrl = "smtpUrl";
    final String port = "25";
    final String user = "user";
    final String password = "password";
    final String dynamicPropertyName = "domibus.alert.mail.smtp.port";
    final String dynamicSmtpPort = "450";
    final Set<String> dynamicPropertySet = Sets.newHashSet(dynamicPropertyName);

    private void setupMailProperties(@Mocked Properties javaMailProperties, @Mocked Predicate predicate) {

        new Expectations() {{
            multiDomainAlertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_URL);
            result = smtpUrl;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT);
            result = port;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_USER);
            result = user;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD);
            result = password;
            multiDomainAlertConfigurationService.getSendEmailActivePropertyName();
            result = "domibus.alert.mail.sending.active";
            domibusPropertyProvider.getOptionalDomainProperty("domibus.alert.mail.sending.active");
            result = true;
            javaMailSender.getJavaMailProperties();
            result = javaMailProperties;
            domibusPropertyProvider.filterPropertiesName(withAny(predicate));
            result = dynamicPropertySet;
            domibusPropertyProvider.getProperty(dynamicPropertyName);
            result = dynamicSmtpPort;
        }};
    }

    @Test
    public void initMailSender(@Mocked final Properties javaMailProperties, @Mocked final Predicate predicate) {

        setupMailProperties(javaMailProperties, predicate);

        mailSender.initMailSender();
        new VerificationsInOrder() {{
            javaMailSender.setHost(smtpUrl);
            times = 1;
            javaMailSender.setPort(25);
            times = 1;
            javaMailSender.setUsername(user);
            times = 1;
            javaMailSender.setPassword(password);
            times = 1;
            javaMailProperties.put("mail.smtp.port", dynamicSmtpPort);
        }};
    }

    @Test
    public void sendMail(@Mocked final Properties javaMailProperties, @Mocked final Predicate predicate,
                         @Mocked MailModel model, @Mocked MimeMessage mimeMessage,
                         @Mocked MimeMessageHelper mimeMessageHelper) throws IOException, MessagingException {

        setupMailProperties(javaMailProperties, predicate);

        new Expectations() {{
            javaMailSender.createMimeMessage();
            result = mimeMessage;
        }};
        mailSender.sendMail(model, "from@test.com", "recipient1@test.com;recipient2@test.com");
        new VerificationsInOrder() {{
            freemarkerConfig.getTemplate(model.getTemplatePath());
            times = 1;
            javaMailSender.send(mimeMessage);
            times = 1;
        }};
    }

    @Test(expected = AlertDispatchException.class)
    public void sendMailTestInvalidTemplate(@Mocked final Properties javaMailProperties, @Mocked final Predicate predicate,
                                            @Mocked MailModel model, @Mocked MimeMessage mimeMessage,
                                            @Mocked MimeMessageHelper mimeMessageHelper) throws IOException, MessagingException {

        setupMailProperties(javaMailProperties, predicate);

        new Expectations() {{
            javaMailSender.createMimeMessage();
            result = mimeMessage;
            freemarkerConfig.getTemplate(anyString);
            result = new TemplateNotFoundException("test", null, "error message");
        }};

        mailSender.sendMail(model, "from@test.com", "recipient1@test.com;recipient2@test.com");
        new VerificationsInOrder() {{
            javaMailSender.send((MimeMessage) any);
            times = 0;
        }};
    }

    @Test(expected = AlertDispatchException.class)
    public void sendMailTestSendMailFailure(@Mocked final Properties javaMailProperties, @Mocked final Predicate predicate,
                                            @Mocked MailModel model, @Mocked MimeMessage mimeMessage,
                                            @Mocked MimeMessageHelper mimeMessageHelper) throws IOException, MessagingException {

        setupMailProperties(javaMailProperties, predicate);

        new Expectations() {{
            javaMailSender.createMimeMessage();
            result = mimeMessage;

            javaMailSender.send(mimeMessage);
            result = new MailSendException("error message");
        }};

        mailSender.sendMail(model, "from@test.com", "recipient1@test.com;recipient2@test.com");
        new VerificationsInOrder() {{
            javaMailSender.send(mimeMessage);
            times = 1;
        }};
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMailIllegalAddresses(@Mocked final Properties javaMailProperties, @Mocked final Predicate predicate,
                                         @Mocked MailModel model, @Mocked MimeMessage mimeMessage,
                                         @Mocked MimeMessageHelper mimeMessageHelper) throws IOException, MessagingException {

        mailSender.sendMail(model, "", "   ");

        new VerificationsInOrder() {{
            javaMailSender.send((MimeMessage) any);
            times = 0;
        }};
    }
}