package eu.domibus.core.replication;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.MessageType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Date;

/**
 * It maps to {@code V_MESSAGE_UI_DIFF} view which tracks differences
 * between native tables and {@code TB_MESSAGE_UI}
 *
 */
@Immutable
@Entity (name = "V_MESSAGE_UI_DIFF")
public class UIMessageDiffEntity {

    @Id
    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "MESSAGE_STATUS")
    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;

    @Column(name = "NOTIFICATION_STATUS")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_TYPE")
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_SUBTYPE")
    private MessageSubtype messageSubtype;

    @Enumerated(EnumType.STRING)
    @Column(name = "MSH_ROLE")
    private MSHRole mshRole;

    @Column(name = "CONVERSATION_ID", nullable = false)
    protected String conversationId;

    @Column(name = "DELETED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    @Column(name = "RESTORED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date restored;

    @Column(name = "FAILED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date failed;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @Column(name = "FROM_ID")
    private String fromId;

    @Column(name = "FROM_SCHEME")
    private String fromScheme;

    @Column(name = "TO_ID")
    private String toId;

    @Column(name = "TO_SCHEME")
    private String toScheme;

    @Column(name = "REF_TO_MESSAGE_ID")
    private String refToMessageId;

    public String getMessageId() {
        return messageId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public MessageSubtype getMessageSubtype() {
        return messageSubtype;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Date getDeleted() {
        return deleted;
    }

    public Date getReceived() {
        return received;
    }

    public Date getRestored() {
        return restored;
    }

    public Date getFailed() {
        return failed;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public int getSendAttemptsMax() {
        return sendAttemptsMax;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public String getFromId() {
        return fromId;
    }

    public String getFromScheme() {
        return fromScheme;
    }

    public String getToId() {
        return toId;
    }

    public String getToScheme() {
        return toScheme;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof UIMessageDiffEntity)) return false;

        UIMessageDiffEntity that = (UIMessageDiffEntity) o;

        return new EqualsBuilder()
                .append(sendAttempts, that.sendAttempts)
                .append(sendAttemptsMax, that.sendAttemptsMax)
                .append(messageId, that.messageId)
                .append(messageStatus, that.messageStatus)
                .append(notificationStatus, that.notificationStatus)
                .append(messageType, that.messageType)
                .append(messageSubtype, that.messageSubtype)
                .append(mshRole, that.mshRole)
                .append(conversationId, that.conversationId)
                .append(deleted, that.deleted)
                .append(received, that.received)
                .append(restored, that.restored)
                .append(failed, that.failed)
                .append(nextAttempt, that.nextAttempt)
                .append(fromId, that.fromId)
                .append(fromScheme, that.fromScheme)
                .append(toId, that.toId)
                .append(toScheme, that.toScheme)
                .append(refToMessageId, that.refToMessageId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageId)
                .append(messageStatus)
                .append(notificationStatus)
                .append(messageType)
                .append(messageSubtype)
                .append(mshRole)
                .append(conversationId)
                .append(deleted)
                .append(received)
                .append(restored)
                .append(failed)
                .append(sendAttempts)
                .append(sendAttemptsMax)
                .append(nextAttempt)
                .append(fromId)
                .append(fromScheme)
                .append(toId)
                .append(toScheme)
                .append(refToMessageId)
                .toHashCode();
    }
}
