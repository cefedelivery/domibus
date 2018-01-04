package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class MessageInfo {

    /**
     * Message's timestamp {@link Date}
     */
    protected Date timestamp;

    /**
     * Message's Identifier {@link String}
     */
    protected String messageId;

    /**
     * Reference to the Identifier of another message {@link String}
     */
    protected String refToMessageId;

    /**
     * Gets Message's timestamp
     * @return Message's timestamp {@link Date}
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets Message's timestamp
     * @param timestamp Message's Timestamp {@link Date}
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets Message's Identifier
     * @return Message's Identifier {@link String}
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets Message's Identifier
     * @param messageId Message's Identifier {@link String}
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets Reference to the Identifier of another message
     * @return Reference to the Identifier of another message {@link String}
     */
    public String getRefToMessageId() {
        return refToMessageId;
    }

    /**
     * Sets Reference to the Identifier of another message
     * @param refToMessageId Reference to the Identifier of another message {@link String}
     */
    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageInfo that = (MessageInfo) o;

        return new EqualsBuilder()
                .append(timestamp, that.timestamp)
                .append(messageId, that.messageId)
                .append(refToMessageId, that.refToMessageId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(timestamp)
                .append(messageId)
                .append(refToMessageId)
                .toHashCode();
    }
}
