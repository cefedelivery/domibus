package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by migueti on 15/03/2017.
 */
@Entity
@Table(name = "tb_message_acknowledge")
@NamedQueries({
        @NamedQuery(name = "MessageAcknowledge.findMessageAcknowledgeByMessageId",
                query = "select messageAcknowledge from MessageAcknowledge messageAcknowledge where messageAcknowledge.messageId = :MESSAGE_ID"),
        @NamedQuery(name = "MessageAcknowledge.findMessageAcknowledgeByFrom",
                query = "select messageAcknowledge from MessageAcknowledge messageAcknowledge where messageAcknowledge.from = :FROM"),
        @NamedQuery(name = "MessageAcknowledge.findMessageAcknowledgeByTo",
                query = "select messageAcknowledge from MessageAcknowledge messageAcknowledge where messageAcknowledge.to = :TO")
})
public class MessageAcknowledge extends AbstractBaseEntity {

    @Column(name = "FK_MESSAGE_ID")
    private String messageId;

    @Column(name = "FROM")
    private String from;

    @Column(name = "TO")
    private String to;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_MESSAGEACKNOWLEDGE")
    private Set<MessageAcknowledgeProperty> properties;

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

    public Set<MessageAcknowledgeProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<MessageAcknowledgeProperty> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAcknowledge messageAcknowledge = (MessageAcknowledge) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageId, messageAcknowledge.getMessageId())
                .append(from, messageAcknowledge.getFrom())
                .append(to, messageAcknowledge.getTo())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
                .appendSuper(super.hashCode())
                .append(messageId)
                .append(from)
                .append(to)
                .toHashCode();
    }
}
