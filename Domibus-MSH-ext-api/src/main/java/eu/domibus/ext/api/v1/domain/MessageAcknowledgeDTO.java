package eu.domibus.ext.api.v1.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author Cosmin Baciu
 */
public class MessageAcknowledgeDTO implements Serializable {

    /**
     * The AS4 message id
     */
    protected String messageId;

    /**
     * Custom properties associated with a message acknowledgment
     */
    protected Map<String, Object> properties;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
