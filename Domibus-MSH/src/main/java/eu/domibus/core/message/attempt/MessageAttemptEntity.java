package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Entity
@Table(name = "TB_SEND_ATTEMPT")
@NamedQueries({
        @NamedQuery(name = "MessageAttemptEntity.findAttemptsByMessageId",
                query = "select attempt from MessageAttemptEntity attempt where attempt.messageId = :MESSAGE_ID")
})
public class MessageAttemptEntity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "START_DATE")
    private Timestamp startDate;

    @Column(name = "END_DATE")
    private Timestamp endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private MessageAttemptStatus status;

    @Column(name = "ERROR")
    private String error;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public MessageAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(MessageAttemptStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAttemptEntity that = (MessageAttemptEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageId, that.messageId)
                .append(startDate, that.startDate)
                .append(endDate, that.endDate)
                .append(status, that.status)
                .append(error, that.error)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageId)
                .append(startDate)
                .append(endDate)
                .append(status)
                .append(error)
                .toHashCode();
    }
}
