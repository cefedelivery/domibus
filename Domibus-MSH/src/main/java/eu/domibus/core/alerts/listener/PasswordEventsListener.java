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

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;



    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'userPasswordImminentExpiration'")
    public void onImminentExpirationEvent(final Event event, @Header(name = "DOMAIN") String domain) {


        LOG.info("onImminentExpirationEvent... domain= "+domain);
        domainContextProvider.setCurrentDomain(domain);

        final AlertEventModuleConfiguration eventConfiguration = multiDomainAlertConfigurationService.getRepetitiveEventConfiguration(AlertType.PASSWORD_IMMINENT_EXPIRATION);
        if (!eventConfiguration.isActive()) {
            return;
        }

        saveEventAndTriggerAlert(event, domain, eventConfiguration.getEventFrequency());
    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'userPasswordExpired'")
    public void onExpiredEvent(final Event event, @Header(name = "DOMAIN") String domain) {


        LOG.info("onExpiredEvent... domain= "+domain);
        domainContextProvider.setCurrentDomain(domain);

        final AlertEventModuleConfiguration eventConfiguration = multiDomainAlertConfigurationService.getRepetitiveEventConfiguration(AlertType.PASSWORD_EXPIRED);
        if (!eventConfiguration.isActive()) {
            return;
        }

        saveEventAndTriggerAlert(event, domain, eventConfiguration.getEventFrequency());
    }

    private void saveEventAndTriggerAlert(Event event, @Header(name = "DOMAIN") String domain, int frequency) {

        //find the corresponding event
        eu.domibus.core.alerts.model.persist.Event entity
                = eventDao.findWithTypeAndPropertyValue(event.getType(), "Source", event.findStringProperty("Source").get());
        if (entity == null) {
            return;
        }

        LOG.info("Event entity found: [{}] ", entity.getEntityId());
        event.setEntityId(entity.getEntityId());

        LocalDate lastAlertDate = entity.getLastAlertDate();
        LocalDate notificationDate = LocalDate.now().minusDays(frequency);

        //check if the alert is sent based on frequency
        if (lastAlertDate != null && !lastAlertDate.isBefore(notificationDate)) {   //alert already sent, do nothing
            return;
        }

        entity.setLastAlertDate(LocalDate.now());
        eventDao.update(entity);
        eventDao.flush();


        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);

        //add another instance of event?????
        eventService.enqueuePasswordExpiredEvent(event);
    }

}
