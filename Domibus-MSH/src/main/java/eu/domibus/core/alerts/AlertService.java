package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Alert;
import eu.domibus.core.alerts.model.Event;
import eu.domibus.core.alerts.model.MailModel;

public interface AlertService {

    Alert createAlertOnEvent(Event event);

    void enqueueAlert(Alert alert);

    MailModel getMailModelForAlert(Alert alert);

    void handleSendAlertStatus(Alert alert);
}
