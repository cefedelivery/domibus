package eu.domibus.core.alerts.listener;

import eu.domibus.core.alerts.service.AlertDispatcherService;
import eu.domibus.core.alerts.model.service.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AlertListener {

    private final static Logger LOG = LoggerFactory.getLogger(AlertListener.class);

    @Autowired
    @Qualifier("alertDispatcher")
    private AlertDispatcherService alertDispatcherService;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'alert'")
    public void onAlert(final Alert alert) {
        LOG.debug("Alert received:[{}]", alert);
        alertDispatcherService.dispatch(alert);
    }


}
