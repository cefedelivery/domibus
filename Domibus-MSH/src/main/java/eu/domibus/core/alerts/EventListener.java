package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private final static Logger LOG = LoggerFactory.getLogger(EventListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'message'")
    public void onEvent(final Event event){
        LOG.debug("Message event received:[{}]",event);
        eventService.enrichMessage(event);
        eventService.persistEvent(event);
        alertService.processEvent(event);
    }
}
