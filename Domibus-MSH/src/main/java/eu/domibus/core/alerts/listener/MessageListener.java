package eu.domibus.core.alerts.listener;

import eu.domibus.core.alerts.AlertService;
import eu.domibus.core.alerts.EventService;
import eu.domibus.core.alerts.model.Alert;
import eu.domibus.core.alerts.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private final static Logger LOG = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'message'")
    public void onMessageEvent(final Event event) {
        LOG.debug("Message event received:[{}]", event);
        eventService.enrichMessage(event);
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }

}
