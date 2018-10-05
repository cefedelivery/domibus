package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.service.AlertDispatcherService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class AlertListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertListener.class);

    @Autowired
    private AlertDispatcherService alertDispatcherService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'alert'")
    public void onAlert(final Alert alert, @Header(name = "DOMAIN", required = false) String domain) {
        if (StringUtils.isNotEmpty(domain)) {
            domainContextProvider.setCurrentDomain(domain);
            LOG.debug("Alert received:[{}] for domain:[{}]", alert, domain);
        } else {
            LOG.debug("Super alert received:[{}]", alert);
        }
        alertDispatcherService.dispatch(alert);
    }


}
