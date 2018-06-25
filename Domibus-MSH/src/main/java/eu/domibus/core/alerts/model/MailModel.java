package eu.domibus.core.alerts.model;

public interface MailModel<T> {

    T getModel();

    String getTemplatePath();

    String getSubject();
}
