package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Entity
@Table(name = "TB_MESSAGE_ACKNW")
@NamedQueries({
        @NamedQuery(name = "MessageAcknowledgement.findMessageAcknowledgementByMessageId",
                query = "select messageAcknowledge from MessageAcknowledgementEntity messageAcknowledge where messageAcknowledge.messageId = :MESSAGE_ID")
})
public class MessageAcknowledgementEntity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "FROM_VALUE")
    private String from;

    @Column(name = "TO_VALUE")
    private String to;

    @Column(name = "CREATE_DATE")
    private Timestamp createDate;

    @Column(name = "CREATE_USER")
    private String createUser;

    @Column(name = "ACKNOWLEDGE_DATE")
    private Timestamp acknowledgeDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_MSG_ACKNOWLEDGE")
    private Set<MessageAcknowledgementProperty> properties;

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

    public Set<MessageAcknowledgementProperty> getProperties() {
        return properties;
    }

    public Map<String, String> getPropertiesAsMap() {
        if (properties == null) {
            return null;
        }
        final HashMap<String, String> hashMap = new HashMap<>();
        for (MessageAcknowledgementProperty property : properties) {
            hashMap.put(property.getName(), property.getValue());
        }
        return hashMap;
    }

    public void setPropertiesWithMap(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        Set<MessageAcknowledgementProperty> acknowledgmentProperties = new HashSet<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            MessageAcknowledgementProperty property = new MessageAcknowledgementProperty();
            property.setName(entry.getKey());
            property.setValue(entry.getValue());
            acknowledgmentProperties.add(property);
        }
        this.properties = acknowledgmentProperties;
    }

    public void setProperties(Set<MessageAcknowledgementProperty> properties) {
        this.properties = properties;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getAcknowledgeDate() {
        return acknowledgeDate;
    }

    public void setAcknowledgeDate(Timestamp acknowledgeDate) {
        this.acknowledgeDate = acknowledgeDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAcknowledgementEntity messageAcknowledgementEntity = (MessageAcknowledgementEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageId, messageAcknowledgementEntity.getMessageId())
                .append(from, messageAcknowledgementEntity.getFrom())
                .append(to, messageAcknowledgementEntity.getTo())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageId)
                .append(from)
                .append(to)
                .toHashCode();
    }
}
