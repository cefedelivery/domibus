package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.MessageEvent;

public interface EventService {

    void saveEvent(MessageEvent messageEvent);

}
