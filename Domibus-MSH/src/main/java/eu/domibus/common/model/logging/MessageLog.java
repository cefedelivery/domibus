package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.ebms3.common.model.MessageType;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Federico Martini
 * @since 3.2
 */
@MappedSuperclass
@DiscriminatorColumn(name = "MESSAGE_TYPE")
public abstract class MessageLog extends AbstractBaseEntity {

    @Column(name = "MESSAGE_STATUS")
    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;

    @Column(name = "NOTIFICATION_STATUS")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "MSH_ROLE")
    private MSHRole mshRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_TYPE")
    private MessageType messageType;

    @Column(name = "MPC")
    private String mpc;

    @Column(name = "BACKEND")
    private String backend;

    @Column(name = "ENDPOINT")
    private String endpoint;
    /**
     * The Date when this message was deleted, A message shall be deleted when one of the following conditions apply:
     * <p/>
     * - An outgoing message has been sent without error eb:Error/@severity failure failure, and an AS4 receipt has been
     * received
     * - An outgoing message has been sent without error eb:Error/@severity failure, and AS4 is disabled
     * - An outgoing message could not be sent and the final AS4 retry has passed
     * - An outgoing message could not be sent and AS4 is disabled (eb:Error/@severity failure, [CORE 6.2.5])
     * <p/>
     * - A received message
     */
    @Column(name = "DELETED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    @Column(name = "DOWNLOADED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date downloaded;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMpc() {
        return this.mpc;
    }

    public void setMpc(final String mpc) {
        this.mpc = mpc;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public void setMessageType(final MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    public MSHRole getMshRole() {
        return this.mshRole;
    }

    public void setMshRole(final MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public Date getDeleted() {
        return this.deleted;
    }

    public void setDeleted(final Date deleted) {
        this.deleted = deleted;
    }

    public Date getReceived() {
        return this.received;
    }

    public void setReceived(final Date received) {
        this.received = received;
    }

    public Date getDownloaded() {
        return this.downloaded;
    }

    public void setDownloaded(final Date downloaded) {
        this.downloaded = downloaded;
    }

    public Date getNextAttempt() {
        return this.nextAttempt;
    }

    public void setNextAttempt(final Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public int getSendAttempts() {
        return this.sendAttempts;
    }

    public void setSendAttempts(final int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public int getSendAttemptsMax() {
        return this.sendAttemptsMax;
    }

    public void setSendAttemptsMax(final int sendAttemptsMax) {
        this.sendAttemptsMax = sendAttemptsMax;
    }

    public NotificationStatus getNotificationStatus() {
        return this.notificationStatus;
    }

    public void setNotificationStatus(final NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public MessageStatus getMessageStatus() {
        return this.messageStatus;
    }

    public void setMessageStatus(final MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(final String backend) {
        this.backend = backend;
    }
}
