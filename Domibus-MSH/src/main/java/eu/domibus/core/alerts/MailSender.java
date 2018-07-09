package eu.domibus.core.alerts;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class MailSender {

    private final static Logger LOG = LoggerFactory.getLogger(MailSender.class);

    private static final String DOMIBUS_ALERT_SENDER_SMTP_URL = "domibus.alert.sender.smtp.url";

    private static final String DOMIBUS_ALERT_SENDER_SMTP_PORT = "domibus.alert.sender.smtp.port";

    private static final String DOMIBUS_ALERT_SENDER_SMTP_USER = "domibus.alert.sender.smtp.user";

    private static final String DOMIBUS_ALERT_SENDER_SMTP_PASSWORD = "domibus.alert.sender.smtp.password";

    private static final String DOMIBUS_ALERT_MAIL = "domibus.alert.mail";

    private static final String MAIL = ".mail";

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @PostConstruct
    void init() {
        final Boolean alertModuleEnabled = multiDomainAlertConfigurationService.isAlertModuleEnabled();
        LOG.debug("Alert module enabled:[{}]", alertModuleEnabled);
        if (alertModuleEnabled) {
            //static properties.
            final String url = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_URL);
            final Integer port = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT));
            final String user = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_USER);
            final String password = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD);

            LOG.debug("Configuring mail server.");
            LOG.debug("Smtp url:[{}]", url);
            LOG.debug("Smtp port:[{}]", port);
            LOG.debug("Smtp user:[{}]", user);

            javaMailSender.setHost(url);
            javaMailSender.setPort(port);
            javaMailSender.setUsername(user);
            javaMailSender.setPassword(password);
            //Non static properties.
            final Properties javaMailProperties = javaMailSender.getJavaMailProperties();
            final Set<String> mailPropertyNames = domibusPropertyProvider.filterPropertiesName(s -> s.startsWith(DOMIBUS_ALERT_MAIL));
            mailPropertyNames.stream().
                    map(domibusPropertyName -> domibusPropertyName.substring(domibusPropertyName.indexOf(MAIL))).
                    forEach(mailPropertyName -> {
                        final String propertyValue = domibusPropertyProvider.getProperty(mailPropertyName);
                        javaMailProperties.put(mailPropertyName, propertyValue);
                        LOG.debug("mail property:[{}] value:[{}]", mailPropertyName, propertyValue);
                    });
        }
    }

    public <T extends MailModel> void sendMail(final T model, final String to, final String from) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            Template template = freemarkerConfig.getTemplate(model.getTemplatePath());
            final Object model1 = model.getModel();
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model1);

            helper.setTo(to);
            helper.setText(html, true);
            helper.setSubject(model.getSubject());
            helper.setFrom(from);
            javaMailSender.send(message);
        } catch (IOException | MessagingException | TemplateException e) {
            LOG.error("Exception while sending mail from[{}] to[{}]", from, to, e);
            throw new AlertDispatchException(e);
        }
    }


}
