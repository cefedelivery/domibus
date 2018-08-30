package eu.domibus.core.replication;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.MessageType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity which maps 1 record of {@code TB_MESSAGE_UI} table
 *
 * @author Catalin Enache
 * @since 4.0
 *
 */
@Entity
@Table(name = "TB_MESSAGE_UI")
@NamedQueries({
        @NamedQuery(name = "UIMessageEntity.findUIMessageByMessageId",
                query = "select uiMessageEntity from UIMessageEntity uiMessageEntity where uiMessageEntity.messageId=:MESSAGE_ID")})
@SqlResultSetMapping(name="updateResult", columns = { @ColumnResult(name = "count")})
@NamedNativeQueries({
        @NamedNativeQuery(
                name    =   "UIMessageEntity.updateMessageStatus",
                query   =   "UPDATE TB_MESSAGE_UI SET MESSAGE_STATUS=?1, DELETED=?2, NEXT_ATTEMPT=?3, FAILED=?4, LAST_MODIFIED=?5 " +
                        " WHERE MESSAGE_ID=?6"
                ,resultSetMapping = "updateResult"
        ),
        @NamedNativeQuery(
                name    =   "UIMessageEntity.updateNotificationStatus",
                query   =   "UPDATE TB_MESSAGE_UI SET NOTIFICATION_STATUS=?1, LAST_MODIFIED2=?2 " +
                        " WHERE MESSAGE_ID=?3"
                ,resultSetMapping = "updateResult"
        ),
        @NamedNativeQuery(
                name    =   "UIMessageEntity.updateMessage",
                query   =   "UPDATE TB_MESSAGE_UI SET MESSAGE_STATUS=?1, DELETED=?2, FAILED=?3, RESTORED=?4, NEXT_ATTEMPT=?5, SEND_ATTEMPTS=?6, SEND_ATTEMPTS_MAX=?7, LAST_MODIFIED=?8 " +
                        " WHERE MESSAGE_ID=?9"
                ,resultSetMapping = "updateResult"
        )
})
public class UIMessageEntity extends AbstractBaseEntity {

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

    //@Version
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED")
    private Date lastModified;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED2")
    private Date lastModified2;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageSubtype getMessageSubtype() {
        return messageSubtype;
    }

    public void setMessageSubtype(MessageSubtype messageSubtype) {
        this.messageSubtype = messageSubtype;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Date getRestored() {
        return restored;
    }

    public void setRestored(Date restored) {
        this.restored = restored;
    }

    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public int getSendAttemptsMax() {
        return sendAttemptsMax;
    }

    public void setSendAttemptsMax(int sendAttemptsMax) {
        this.sendAttemptsMax = sendAttemptsMax;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromScheme() {
        return fromScheme;
    }

    public void setFromScheme(String fromScheme) {
        this.fromScheme = fromScheme;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getToScheme() {
        return toScheme;
    }

    public void setToScheme(String toScheme) {
        this.toScheme = toScheme;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date version) {
        this.lastModified = version;
    }

    public Date getLastModified2() {
        return lastModified2;
    }

    public void setLastModified2(Date lastModified2) {
        this.lastModified2 = lastModified2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof UIMessageEntity)) return false;

        UIMessageEntity that = (UIMessageEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
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
                .appendSuper(super.hashCode())
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
