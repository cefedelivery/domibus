package eu.domibus.ext.services.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

/**
 * DTO class for the Message Acknowledgements Service.
 *
 * Contains the details of a message acknowledgement.
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgementDTO implements Serializable {

    /**
     * Property indicating the source of the message acknowledgment(C3 or the backend name)
     */
    public static final String FROM = "FROM";

    /**
     * Property indicating the destination of the message acknowledgment(C3 or the backend name))
     */
    public static final String TO = "TO";

    /**
     * The message id associated with the message acknowledgement
     */
    protected String messageId;

    /**
     * Timestamp of the acknowledged time
     */
    protected Timestamp acknowledgeTimestamp;

    /**
     * Properties of the message acknowledgment
     */
    protected Map<String, String> properties;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Timestamp getAcknowledgeTimestamp() {
        return acknowledgeTimestamp;
    }

    public void setAcknowledgeTimestamp(Timestamp acknowledgeTimestamp) {
        this.acknowledgeTimestamp = acknowledgeTimestamp;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}