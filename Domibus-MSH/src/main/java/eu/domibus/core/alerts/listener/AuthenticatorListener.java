package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class AuthenticatorListener {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticatorListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'loginFailure'")
    public void onLoginFailure(final Event event,final @Header(name = "DOMAIN") String domain) {
        domainContextProvider.setCurrentDomain(domain);
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);

    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'accountDisabled'")
    public void onAccountDisabled(final Event event,@Header(name = "DOMAIN") String domain) {
        domainContextProvider.setCurrentDomain(domain);
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);

    }

}
