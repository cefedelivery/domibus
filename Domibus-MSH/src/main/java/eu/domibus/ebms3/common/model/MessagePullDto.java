package eu.domibus.ebms3.common.model;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class MessagePullDto {
    private String messageId;
    private Date messageReceived;

    public MessagePullDto(String messageId, Date messageReceived) {
        this.messageId = messageId;
        this.messageReceived = messageReceived;
    }

    public String getMessageId() {
        return messageId;
    }

    public Date getMessageReceived() {
        return messageReceived;
    }
}
