package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.AlertEventModuleConfiguration;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.EventServiceImpl;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
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
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private EventDao eventDao;


    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'userPasswordImminentExpiration'")
    public void onImminentExpirationEvent(final Event event, @Header(name = "DOMAIN") String domain) {

        LOG.info("onImminentExpirationEvent... domain= "+domain);

        saveEventAndTriggerAlert(event, domain);
    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'userPasswordExpired'")
    public void onExpiredEvent(final Event event, @Header(name = "DOMAIN") String domain) {

        LOG.info("onExpiredEvent... domain= "+domain);

        saveEventAndTriggerAlert(event, domain);
    }

    private void saveEventAndTriggerAlert(Event event , @Header(name = "DOMAIN") String domain ) {

        domainContextProvider.setCurrentDomain(domain);

        //find the corresponding event
        eu.domibus.core.alerts.model.persist.Event entity = eventDao.read(event.getEntityId());
        if (entity == null) {
            return;
        }

        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }

}
