package eu.domibus.common;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class MessageStatusChangeEvent implements Serializable {

    protected String messageId;
    protected MessageStatus fromStatus;
    protected MessageStatus toStatus;
    protected Timestamp changeTimestamp;

    protected Map<String, Object> properties = new HashMap<>();

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

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
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
