package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Qualifier("alertDispatcher")
@Service
public class AlertDispatcherImpl implements AlertDispatcher {

    private final static Logger LOG = LoggerFactory.getLogger(AlertDispatcherImpl.class);

    @Autowired
    private MailAlertDispatcher mailAlertDispatcher;

    public void dispatch(Alert alert) {
        mailAlertDispatcher.dispatch(alert);
    }

}
