package eu.domibus.ext.services.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

/**
 * DTO class used by {@link eu.domibus.ext.services.MessageAcknowledgeService}.
 *
 * <p>Contains the details of a message acknowledgement.</p>
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgementDTO implements Serializable {

    /**
     * The id of the message acknowledgement
     */
    protected String id;

    /**
     * The message id associated with the message acknowledgement
     */
    protected String messageId;

    /**
     * The source of the acknowledgement(eg: C3 or backend name)
     */
    protected String from;

    /**
     * The target of the acknowledgement(eg: C3 or backend name)
     */
    protected String to;

    /**
     * Timestamp of the acknowledged time
     */
    protected Timestamp acknowledgeTimestamp;

    /**
     * Custom properties of the message acknowledgment (like FROM and TO)
     */
    protected Map<String, String> properties;

    /**
     * Timestamp of the acknowledgement creation time(when it has been saved into the persistence layer)
     */
    protected Timestamp createdTimestamp;


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

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}