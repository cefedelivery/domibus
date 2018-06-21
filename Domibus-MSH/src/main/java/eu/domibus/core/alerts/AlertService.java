package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Event;

public interface AlertService {

    void processEvent(Event event);
}
