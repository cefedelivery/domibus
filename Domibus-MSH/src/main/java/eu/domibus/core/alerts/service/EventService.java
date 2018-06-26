package eu.domibus.core.alerts.service;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.service.Event;

public interface EventService {

    void enqueueMessageEvent(String messageId, MessageStatus oldStatus, MessageStatus newStatus, MSHRole role);

    void persistEvent(Event event);

    void enrichMessageEvent(Event event);

}
