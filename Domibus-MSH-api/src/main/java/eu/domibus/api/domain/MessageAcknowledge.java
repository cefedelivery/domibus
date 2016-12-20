package eu.domibus.api.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author baciu
 */
public class MessageAcknowledge implements Serializable {

    protected String messageId;

    private String username;

    private String originalUser;

    protected Date createDate;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
