package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class CertificateListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(CertificateListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'certificateImminentExpiration'")
    public void onImminentRevocationCertificateEvent(final Event event, @Header(name = "DOMAIN") String domain) {
        saveEventAndTriggerAlert(event, domain);

    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'certificateExpired'")
    public void onRevokedCertificateEvent(final Event event, @Header(name = "DOMAIN") String domain) {
        saveEventAndTriggerAlert(event, domain);

    }

    private void saveEventAndTriggerAlert(Event event, @Header(name = "DOMAIN") String domain) {
        LOG.debug("Certificate event:[{}] for domain:[{}]", event, domain);
        domainContextProvider.setCurrentDomain(domain);
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }

}
