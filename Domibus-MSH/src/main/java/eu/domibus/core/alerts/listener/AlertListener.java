package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.service.AlertDispatcherService;
import eu.domibus.core.alerts.model.service.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class AlertListener {

    private final static Logger LOG = LoggerFactory.getLogger(AlertListener.class);

    @Autowired
    @Qualifier("alertDispatcher")
    private AlertDispatcherService alertDispatcherService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'alert'")
    public void onAlert(final Alert alert,@Header(name = "DOMAIN") String domain) {
        domainContextProvider.setCurrentDomain(domain);
        LOG.debug("Alert received:[{}]", alert);
        alertDispatcherService.dispatch(alert);
    }


}
