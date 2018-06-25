package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Alert;
import eu.domibus.core.alerts.model.MailModel;
import eu.domibus.core.alerts.model.persist.AlertStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailAlertDispatcher implements AlertDispatcher {

    private final static Logger LOG = LoggerFactory.getLogger(MailAlertDispatcher.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private MailSender mailSender;

    @Override
    public void dispatch(Alert alert) {
        final MailModel mailModelForAlert = alertService.getMailModelForAlert(alert);
        try {
            alert.setAlertStatus(AlertStatus.FAILED);
            mailSender.sendMail(mailModelForAlert, "to", "from");
            alert.setAlertStatus(AlertStatus.SENT);
        } finally {
            alertService.handleSendAlertStatus(alert);
        }
    }

}
