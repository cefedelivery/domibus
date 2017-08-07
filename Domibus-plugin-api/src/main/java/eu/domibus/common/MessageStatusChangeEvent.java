package eu.domibus.common;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class MessageStatusChangeEvent implements Serializable {

    protected String messageId;
    protected MessageStatus fromStatus;
    protected MessageStatus toStatus;
    protected Timestamp changeTimestamp;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(MessageStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public MessageStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(MessageStatus toStatus) {
        this.toStatus = toStatus;
    }

    public Timestamp getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(Timestamp changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageId", messageId)
                .append("fromStatus", fromStatus)
                .append("toStatus", toStatus)
                .append("changeTimestamp", changeTimestamp)
                .toString();
    }
}
