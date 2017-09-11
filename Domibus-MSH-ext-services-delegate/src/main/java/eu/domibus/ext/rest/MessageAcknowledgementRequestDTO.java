package eu.domibus.ext.rest;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;


public class MessageAcknowledgementRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * The message id associated with the message acknowledgement
     */
    private String messageId;

    /**
     * Timestamp of the acknowledge time
     */
    private Timestamp acknowledgeDate;

    /**
     * Custom properties of the message acknowledgment
     */
    private Map<String, String> properties;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Timestamp getAcknowledgeDate() {
        return acknowledgeDate;
    }

    public void setAcknowledgeDate(Timestamp acknowledgeDate) {
        this.acknowledgeDate = acknowledgeDate;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public static long getSerialVersionUID() {

        return serialVersionUID;
    }
}
