package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.MailModel;

public interface AlertService {

    Alert createAlertOnEvent(Event event);

    void enqueueAlert(Alert alert);

    MailModel getMailModelForAlert(Alert alert);

    void handleAlertStatus(Alert alert);

    void retry();
}
