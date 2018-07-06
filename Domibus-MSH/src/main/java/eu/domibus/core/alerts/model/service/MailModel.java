package eu.domibus.core.alerts.model.service;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface MailModel<T> {

    T getModel();

    String getTemplatePath();

    String getSubject();
}
