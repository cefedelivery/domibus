package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.alerts.DomibusEventException;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class EventListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(EventListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private JsonUtil jsonUtil;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "JMSType = 'event'")
    public void onLoginFailure(final MapMessage message) {
        String eventType = null;
        String eventBody = null;
        String domain = null;
        try {
            eventType = message.getStringProperty("eventType");
            eventBody = message.getStringProperty("eventBody");
            domain = message.getStringProperty(MessageConstants.DOMAIN);
        } catch (JMSException e) {
            LOG.error("Could not get properties from the JMS message", e);
            throw new DomibusEventException(e);
        }
        if (StringUtils.isBlank(eventBody)) {
            LOG.error("Could not process event: event body is empty");
            return;
        }
        LOG.debug("Event received with type:[{}], [{}]", eventType, eventBody);

        Event event = jsonUtil.readValue(eventBody, Event.class);
        saveEventAndTriggerAlert(event, domain, eventType);
    }

    private void saveEventAndTriggerAlert(Event event, String domain, String eventType) {
        domainContextProvider.setCurrentDomain(domain);
        if (StringUtils.equalsIgnoreCase(eventType, "message")) {
            LOG.debug("Enriching message event");
            eventService.enrichMessageEvent(event);
        }
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }

}
