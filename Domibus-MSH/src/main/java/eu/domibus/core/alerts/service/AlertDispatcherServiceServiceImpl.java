package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Qualifier("alertDispatcher")
@Service
public class AlertDispatcherServiceServiceImpl implements AlertDispatcherService {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(AlertDispatcherServiceServiceImpl.class);

    @Autowired
    private MailAlertDispatcherServiceImpl mailAlertDispatcher;

    public void dispatch(Alert alert) {
        mailAlertDispatcher.dispatch(alert);
    }

}
