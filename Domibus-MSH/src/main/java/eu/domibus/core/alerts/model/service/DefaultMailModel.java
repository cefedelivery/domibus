package eu.domibus.core.alerts.model.service;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class DefaultMailModel<T> implements MailModel<T> {

    private final T model;

    private final String templatePath;

    private final String subject;

    public DefaultMailModel(final T model, final String templatePath, final String subject) {
        this.model = model;
        this.templatePath = templatePath;
        this.subject = subject;
    }

    @Override
    public T getModel() {
        return model;
    }

    @Override
    public String getTemplatePath() {
        return templatePath;
    }

    @Override
    public String getSubject() {
        return subject;
    }
}
