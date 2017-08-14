package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageLogInfo {

    private String conversationId;

    private String fromPartyId;

    private String toPartyId;

    private String originalSender;

    private String finalRecipient;

    private String refToMessageId;

    final private String messageId;

    final private MessageStatus messageStatus;

    final private NotificationStatus notificationStatus;

    final private MSHRole mshRole;

    final private MessageType messageType;

    final private Date deleted;

    final private Date received;

    final private int sendAttempts;

    final private int sendAttemptsMax;

    final private Date nextAttempt;


    public MessageLogInfo(final String messageId,
                          final MessageStatus messageStatus,
                          final NotificationStatus notificationStatus,
                          final MSHRole mshRole,
                          final MessageType messageType,
                          final Date deleted,
                          final Date received,
                          final int sendAttempts,
                          final int sendAttemptsMax,
                          final Date nextAttempt,
                          final String conversationId,
                          final String fromPartyId,
                          final String toPartyId,
                          final String originalSender,
                          final String finalRecipient,
                          final String refToMessageId) {
        //message log information.
        this.messageId = messageId;
        this.messageStatus = messageStatus;
        this.notificationStatus = notificationStatus;
        this.mshRole = mshRole;
        this.messageType = messageType;
        this.deleted = deleted;
        this.received = received;
        this.sendAttempts = sendAttempts;
        this.sendAttemptsMax = sendAttemptsMax;
        this.nextAttempt = nextAttempt;
        //message information UserMessage/SignalMessage
        this.conversationId = conversationId;
        this.fromPartyId = fromPartyId;
        this.toPartyId = toPartyId;
        this.originalSender = originalSender;
        this.finalRecipient = finalRecipient;
        this.refToMessageId = refToMessageId;
    }



    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getFromPartyId() {
        return fromPartyId;
    }

    public void setFromPartyId(String fromPartyId) {
        this.fromPartyId = fromPartyId;
    }

    public String getToPartyId() {
        return toPartyId;
    }

    public void setToPartyId(String toPartyId) {
        this.toPartyId = toPartyId;
    }

    public String getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(String originalSender) {
        this.originalSender = originalSender;
    }

    public String getFinalRecipient() {
        return finalRecipient;
    }

    public void setFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Date getDeleted() {
        return deleted;
    }

    public Date getReceived() {
        return received;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageLogInfo that = (MessageLogInfo) o;

        return new EqualsBuilder()
                .append(messageId, that.messageId)
                .append(messageStatus, that.messageStatus)
                .append(notificationStatus, that.notificationStatus)
                .append(mshRole, that.mshRole)
                .append(messageType, that.messageType)
                .append(deleted, that.deleted)
                .append(received, that.received)
                .append(sendAttempts, that.sendAttempts)
                .append(sendAttemptsMax, that.sendAttemptsMax)
                .append(nextAttempt, that.nextAttempt)
                .append(conversationId, that.conversationId)
                .append(fromPartyId, that.fromPartyId)
                .append(toPartyId, that.toPartyId)
                .append(originalSender, that.originalSender)
                .append(finalRecipient, that.finalRecipient)
                .append(refToMessageId, that.refToMessageId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageId)
                .append(messageStatus)
                .append(notificationStatus)
                .append(mshRole)
                .append(messageType)
                .append(deleted)
                .append(received)
                .append(sendAttempts)
                .append(sendAttemptsMax)
                .append(nextAttempt)
                .append(conversationId)
                .append(fromPartyId)
                .append(toPartyId)
                .append(originalSender)
                .append(finalRecipient)
                .append(refToMessageId)
                .toHashCode();
    }


}
