package eu.domibus.core.alerts.model.service;

public interface MailModel<T> {

    T getModel();

    String getTemplatePath();

    String getSubject();
}
