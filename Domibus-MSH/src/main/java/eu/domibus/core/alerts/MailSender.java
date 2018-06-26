package eu.domibus.core.alerts;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.service.MailModel;
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

import static eu.domibus.core.alerts.model.service.Alert.DOMIBUS_ALERT_ACTIVE;

@Component
public class MailSender {

    private final static Logger LOG = LoggerFactory.getLogger(MailSender.class);

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @PostConstruct
    void init() {
        final Boolean alertModuleEnabled = Boolean.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_ACTIVE));
        LOG.debug("Alert module enabled:[{}]", alertModuleEnabled);
        if (alertModuleEnabled) {
            //static properties.
            final String url = domibusPropertyProvider.getProperty("domibus.alert.sender.smtp.url");
            final Integer port = Integer.valueOf(domibusPropertyProvider.getProperty("domibus.alert.sender.smtp.port"));
            final String user = domibusPropertyProvider.getProperty("domibus.alert.sender.smtp.user");
            final String password = domibusPropertyProvider.getProperty("domibus.alert.sender.smtp.password");
            LOG.debug("Smtp url:[{}]", url);
            LOG.debug("Smtp port:[{}]", port);
            LOG.debug("Smtp user:[{}]", user);

            javaMailSender.setHost(url);
            javaMailSender.setPort(port);
            javaMailSender.setUsername(user);
            javaMailSender.setPassword(password);
            //Non static properties.
            final Properties javaMailProperties = javaMailSender.getJavaMailProperties();
            final Set<String> mailPropertyNames = domibusPropertyProvider.filterPropertiesName(s -> s.startsWith("domibus.alert.mail"));
            mailPropertyNames.stream().
                    map(domibusPropertyName -> domibusPropertyName.substring(domibusPropertyName.indexOf(".mail"))).
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
