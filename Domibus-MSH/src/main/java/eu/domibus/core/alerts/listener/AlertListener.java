package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.alerts.DomibusEventException;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.service.AlertDispatcherService;
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
public class AlertListener {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(AlertListener.class);

    @Autowired
    private AlertDispatcherService alertDispatcherService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private JsonUtil jsonUtil;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "JMSType = 'alert'")
    public void onAlert(final MapMessage message) {
        String alertBody = null;
        String domain = null;
        try {
            alertBody = message.getStringProperty("alertBody");
            domain = message.getStringProperty(MessageConstants.DOMAIN);
        } catch (JMSException e) {
            LOG.error("Could not get properties from the JMS message", e);
            throw new DomibusEventException(e);
        }
        if (StringUtils.isBlank(alertBody)) {
            LOG.error("Could not process alert: alert body is empty");
            return;
        }
        LOG.debug("Alert received:[{}]", alertBody);

        domainContextProvider.setCurrentDomain(domain);
        Alert alert = jsonUtil.readValue(alertBody, Alert.class);
        alertDispatcherService.dispatch(alert);
    }
}
