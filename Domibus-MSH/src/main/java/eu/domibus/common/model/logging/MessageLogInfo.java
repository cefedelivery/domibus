package eu.domibus.common.model.logging;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageLogInfo {

    private UserMessageLog userMessageLog;

    private SignalMessageLog signalMessageLog;

    private String conversationId;

    private String fromPartyId;

    private String toPartyId;

    private String originalSender;

    private String finalRecipient;

    private String refToMessageId;


    public MessageLogInfo(UserMessageLog userMessageLog, String conversationId, String fromPartyId, String toPartyId, String originalSender, String finalRecipient, String refToMessageId) {
        this.userMessageLog = userMessageLog;
        this.conversationId = conversationId;
        this.fromPartyId = fromPartyId;
        this.toPartyId = toPartyId;
        this.originalSender = originalSender;
        this.finalRecipient = finalRecipient;
        this.refToMessageId = refToMessageId;
    }

    public MessageLogInfo(SignalMessageLog signalMessageLog, String fromPartyId, String toPartyId, String originalSender, String finalRecipient, String refToMessageId) {
        this.signalMessageLog = signalMessageLog;
        this.fromPartyId = fromPartyId;
        this.toPartyId = toPartyId;
        this.originalSender = originalSender;
        this.finalRecipient = finalRecipient;
        this.refToMessageId = refToMessageId;
    }

    public UserMessageLog getUserMessageLog() {
        return userMessageLog;
    }

    public void setUserMessageLog(UserMessageLog userMessageLog) {
        this.userMessageLog = userMessageLog;
    }

    public SignalMessageLog getSignalMessageLog() {
        return signalMessageLog;
    }

    public void setSignalMessageLog(SignalMessageLog signalMessageLog) {
        this.signalMessageLog = signalMessageLog;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageLogInfo that = (MessageLogInfo) o;

        return new EqualsBuilder()
                .append(userMessageLog, that.userMessageLog)
                .append(signalMessageLog, that.signalMessageLog)
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
                .append(userMessageLog)
                .append(signalMessageLog)
                .append(conversationId)
                .append(fromPartyId)
                .append(toPartyId)
                .append(originalSender)
                .append(finalRecipient)
                .append(refToMessageId)
                .toHashCode();
    }
}
