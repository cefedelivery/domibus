package eu.domibus.core.alerts;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.Event;

public interface EventService {

    void sendMessageEvent(String messageId, MessageStatus oldStatus, MessageStatus newStatus, MSHRole role);

    Event enrichMessageEvent(Event event);
}
