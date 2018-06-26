package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.Alert;

public interface AlertDispatcherService {

    void dispatch(Alert alert);

}
