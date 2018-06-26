package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.service.Alert;

/**
 * Strategy to determine the type of alert level should be processed for a given alert.
 */
public interface AlertLevelStrategy {

    /**
     * Return the alert level correponding to the alert.
     * @param alert
     * @return the alert level.
     */
    AlertLevel getAlertLevel(Alert alert);

}
