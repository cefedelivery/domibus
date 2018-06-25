package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Alert;

public interface AlertDispatcher {

    void dispatch(Alert alert);

}
