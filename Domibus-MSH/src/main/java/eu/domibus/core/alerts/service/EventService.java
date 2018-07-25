package eu.domibus.core.alerts.service;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.service.Event;

import java.util.Date;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface EventService {

    /**
     * Will create a message status change event with the given parameter and enqueue it to the alert/event monitoring queue.
     * @param messageId the id of the monitored message.
     * @param oldStatus the old status of the message.
     * @param newStatus the new status of the message.
     * @param role the role of the access point.
     */
    void enqueueMessageEvent(String messageId, MessageStatus oldStatus, MessageStatus newStatus, MSHRole role);

    /**
     * Will create login failure event and enqueue it to the alert/event monitoring queue.
     * @param userName the user name that had a failure login
     * @param loginTime the login failure time.
     * @param accountDisabled whether the account has been disable or not.
     */
    void enqueueLoginFailureEvent(
            String userName,
            Date loginTime,
            boolean accountDisabled);


    /**
     * Will create a account disabled event and enqueue it to the alert/event monitoring queue.
     * @param userName the user name that had a failure login
     * @param accountDisabledTime the account disabled time.
     * @param accountDisabled whether the account has been disable or not.
     */
    void enqueueAccountDisabledEvent(
            String userName,
            Date accountDisabledTime,
            boolean accountDisabled);

    /**
     * Will create a certificate imminent expiration event and enqueue it to the alert/event monitoring queue.
     *
     * @param accessPoint the access point at which the certificate will expire.
     * @param alias the alias of the certificate.
     * @param expirationDate the expiration date.
     */
    void enqueueImminentCertificateExpirationEvent(String accessPoint, String alias, Date expirationDate);

    /**
     * Will create a certificate expired event and enqueue it to the alert/event monitoring queue.
     *
     * @param accessPoint the access point at which the certificate will expire.
     * @param alias the alias of the certificate.
     * @param expirationDate the expiration date.
     */
    void enqueueCertificateExpiredEvent(String accessPoint, String alias, Date expirationDate);

    /**
     * Save an event.
     * @param event the event to save.
     */
    void persistEvent(Event event);

    /**
     * Will enrich a message status change event with potential EBMS error details.
     * @param event the even to enrich.
     */
    void enrichMessageEvent(Event event);

}
