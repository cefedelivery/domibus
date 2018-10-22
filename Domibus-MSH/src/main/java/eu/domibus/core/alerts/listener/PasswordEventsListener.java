package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class PasswordEventsListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PasswordEventsListener.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private EventDao eventDao;


    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'userPasswordImminentExpiration' or selector = 'userPasswordExpired'")
    public void onPasswordEvent(final Event event, @Header(name = "DOMAIN", required = false) String domain) {

        saveEventAndTriggerAlert(event, domain);

    }

    private void saveEventAndTriggerAlert(Event event, String domain) {
        if (domain == null)
            domainContextProvider.clearCurrentDomain();
        else
            domainContextProvider.setCurrentDomain(domain);

        //find the corresponding persisted event
        eu.domibus.core.alerts.model.persist.Event entity = eventDao.read(event.getEntityId());
        if (entity != null) {
            final Alert alertOnEvent = alertService.createAlertOnEvent(event);
            alertService.enqueueAlert(alertOnEvent);
        }
    }

}
