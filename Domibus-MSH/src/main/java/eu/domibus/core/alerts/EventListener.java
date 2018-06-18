package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.Event;

public interface EventListener<E extends Event> {

    void onEvent(E e);
}
