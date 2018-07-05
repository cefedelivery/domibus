package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AlertConfiguration {

    String getMailSubject();

    boolean isActive();

    AlertLevel getAlertLevel(final Alert alert);

}
