package eu.domibus.core.alerts.model.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.alerts.model.common.AlertLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class CertificateScannerConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(CertificateScannerConfiguration.class);


    private final Boolean imminentExpirationActive;
    private Integer imminentExpirationDelay;
    private Integer imminentExpirationFrequency;
    private AlertLevel imminentExpirationAlertLevel;

    private final Boolean revocatedActive;
    private Integer revocatedFrequency;
    private Integer revocatedDuration;
    private AlertLevel revocationLevel;

    public CertificateScannerConfiguration(Boolean imminentExpirationActive, Boolean revocatedActive) {
        this.imminentExpirationActive = imminentExpirationActive;
        this.revocatedActive = revocatedActive;
    }

    public CertificateScannerConfiguration(Boolean imminentExpirationActive, Integer imminentExpirationDelay, Integer imminentExpirationFrequency, AlertLevel imminentExpirationAlertLevel, Boolean revocatedActive, Integer revocatedFrequency, Integer revocatedDuration, AlertLevel revocationLevel) {
        this(imminentExpirationActive,revocatedActive);
        this.imminentExpirationDelay = imminentExpirationDelay;
        this.imminentExpirationFrequency = imminentExpirationFrequency;
        this.imminentExpirationAlertLevel = imminentExpirationAlertLevel;
        this.revocatedFrequency = revocatedFrequency;
        this.revocatedDuration = revocatedDuration;
        this.revocationLevel = revocationLevel;
    }
}
