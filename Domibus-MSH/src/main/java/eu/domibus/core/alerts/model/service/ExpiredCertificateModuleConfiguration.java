package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ExpiredCertificateModuleConfiguration implements AlertModuleConfiguration {

    private static final  Logger LOG = DomibusLoggerFactory.getLogger(ExpiredCertificateModuleConfiguration.class);

    private final Boolean expiredActive;

    private Integer expiredFrequency;

    private Integer expiredDuration;

    private AlertLevel expiredLevel;

    private String expiredMailSubject;

    public ExpiredCertificateModuleConfiguration() {
        this.expiredActive = false;
    }

    public ExpiredCertificateModuleConfiguration(
            Integer expiredFrequency,
            Integer expiredDuration,
            AlertLevel expiredLevel,
            String expiredMailSubject) {
        this.expiredActive=true;
        this.expiredFrequency = expiredFrequency;
        this.expiredDuration = expiredDuration;
        this.expiredLevel = expiredLevel;
        this.expiredMailSubject = expiredMailSubject;
    }

    public Integer getExpiredFrequency() {
        return expiredFrequency;
    }

    public Integer getExpiredDuration() {
        return expiredDuration;
    }

    @Override
    public String getMailSubject() {
        return expiredMailSubject;
    }

    @Override
    public boolean isActive() {
        return expiredActive;
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        final AlertType certImminentExpiration = AlertType.CERT_EXPIRED;
        if (certImminentExpiration != alert.getAlertType()) {
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]", alert.getAlertType(), certImminentExpiration);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return expiredLevel;

    }
}

