package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.Alert;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface AlertMethod {

    void sendAlert(Alert alert);
}
