package eu.domibus.api.acknowledge;

import java.util.Date;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgement {

    /**
     * The id of the message acknowledgement
     */
    private Integer id;

    /**
     * The message id associated with the message acknowledgement
     */
    private String messageId;

    /**
     * The source of the acknowledgement(eg: C3 or backend name)
     */
    private String from;

    /**
     * The target of the acknowledgement(eg: C3 or backend name)
     */
    private String to;

    /**
     * Custom properties of the message acknowledgment (like FROM and TO)
     */
    private Map<String, Object> properties;

    /**
     * Timestamp of the acknowledged time
     */
    private Date acknowledged;

    /**
     * Timestamp of the acknowledgement creation time(when it has been saved into the persistence layer)
     */
    private Date created;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Date getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Date acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
