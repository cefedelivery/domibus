package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ImminentExpirationCertificateConfiguration implements AlertConfiguration{

    private final static Logger LOG = LoggerFactory.getLogger(ImminentExpirationCertificateConfiguration.class);

    private final Boolean imminentExpirationActive;

    private Integer imminentExpirationDelay;

    private Integer imminentExpirationFrequency;

    private AlertLevel imminentExpirationAlertLevel;

    private String imminentExpirationMailSubject;

    public ImminentExpirationCertificateConfiguration(Boolean imminentExpirationActive) {
        this.imminentExpirationActive = imminentExpirationActive;
    }

    public ImminentExpirationCertificateConfiguration(
            Boolean imminentExpirationActive,
            Integer imminentExpirationDelay,
            Integer imminentExpirationFrequency,
            AlertLevel imminentExpirationAlertLevel,
            String imminentExpirationMailSubject) {
        this(imminentExpirationActive);
        this.imminentExpirationDelay = imminentExpirationDelay;
        this.imminentExpirationFrequency = imminentExpirationFrequency;
        this.imminentExpirationAlertLevel = imminentExpirationAlertLevel;
        this.imminentExpirationMailSubject = imminentExpirationMailSubject;
    }

    public Boolean getImminentExpirationActive() {
        return imminentExpirationActive;
    }

    public Integer getImminentExpirationDelay() {
        return imminentExpirationDelay;
    }

    public Integer getImminentExpirationFrequency() {
        return imminentExpirationFrequency;
    }

    @Override
    public String getMailSubject() {
        return imminentExpirationMailSubject;
    }

    @Override
    public boolean isActive() {
        return imminentExpirationActive;
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        final AlertType certImminentExpiration = AlertType.CERT_IMMINENT_EXPIRATION;
        if(certImminentExpiration !=alert.getAlertType()){
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]",alert.getAlertType(), certImminentExpiration);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return imminentExpirationAlertLevel;
    }


}
