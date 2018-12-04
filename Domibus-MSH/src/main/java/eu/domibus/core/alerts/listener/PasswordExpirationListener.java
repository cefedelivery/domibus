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

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class PasswordExpirationListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PasswordExpirationListener.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private EventDao eventDao;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'PASSWORD_EXPIRATION'")
    //TODO it would be nice to use here eventType.getQueueSelector() instead of hardcoded string
    // Intentionally used just one selector value for all 4 types of events
    public void onPasswordEvent(final Event event, @Header(name = "DOMAIN", required = false) String domain) {

        triggerAlert(event, domain);

    }

    private void triggerAlert(Event event, String domain) {
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
