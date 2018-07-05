package eu.domibus.core.alerts.service;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.service.Event;

import java.util.Date;

public interface EventService {

    /**
     * Will create an event linked to a message status change with the given parameter and enqueue it to the alert/event monitoring queue.
     * @param messageId the id of the monitored message.
     * @param oldStatus the oldstatus of the message.
     * @param newStatus the new status of the message.
     * @param role the role of the accesspoint.
     */
    void enqueueMessageEvent(String messageId, MessageStatus oldStatus, MessageStatus newStatus, MSHRole role);

    void enqueueLoginFailureEvent(
            String userName,
            Date loginTime,
            boolean accountDisabled);

    void enqueueAccountDisabledEvent(
            String userName,
            Date loginTime,
            boolean accountDisabled);

    void enqueueImminentCertificateExpirationEvent(String accessPoint, String alias, Date expirationDate);

    void enqueueCertificateExpiredEvent(String accessPoint, String alias, Date expirationDate);

    /**
     * Save an event.
     * @param event the event to save.
     */
    void persistEvent(Event event);

    /**
     * Will enrich a message status change event with potential ebms error details.
     * @param event the even to enrich.
     */
    void enrichMessageEvent(Event event);

}
