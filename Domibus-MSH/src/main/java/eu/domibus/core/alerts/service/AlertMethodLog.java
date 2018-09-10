package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class AlertMethodLog implements AlertMethod {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertMethodLog.class);

    @Override
    public void sendAlert(Alert alert) {
        LOG.info("Logging alert [{}]", alert);
    }
}
