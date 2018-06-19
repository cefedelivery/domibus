package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageEventListener{

    private final static Logger LOG = LoggerFactory.getLogger(MessageEventListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory",
            destination = "alertMessageQueue",
            selector = "eventType = 'message'")
    public void onEvent(final Event event){
        final Event enrichMessageEvent = eventService.enrichMessageEvent(event);
        alertService.onEvent(enrichMessageEvent.getEntityId());
    }
}
