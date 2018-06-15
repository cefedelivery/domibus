package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO class for the Message Info
 *
 * It stores some information about a message
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Message's timestamp {@link Date}
     */
    private Date timestamp;

    /**
     * Message's Identifier {@link String}
     */
    private String messageId;

    /**
     * Reference to the Identifier of another message {@link String}
     */
    private String refToMessageId;

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
    public String toString() {
        return new ToStringBuilder(this)
                .append("timestamp", timestamp)
                .append("messageId", messageId)
                .append("refToMessageId", refToMessageId)
                .toString();
    }
}
