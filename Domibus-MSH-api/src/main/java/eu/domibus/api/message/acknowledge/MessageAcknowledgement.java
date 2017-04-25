package eu.domibus.api.message.acknowledge;

import java.sql.Timestamp;
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
    private Map<String, String> properties;

    /**
     * Timestamp of the acknowledged time
     */
    private Timestamp acknowledgeDate;

    /**
     * Timestamp of the acknowledgement creation time(when it has been saved into the persistence layer)
     */
    private Timestamp createDate;

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    private String createUser;

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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Timestamp getAcknowledgeDate() {
        return acknowledgeDate;
    }

    public void setAcknowledgeDate(Timestamp acknowledgeDate) {
        this.acknowledgeDate = acknowledgeDate;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }
}
