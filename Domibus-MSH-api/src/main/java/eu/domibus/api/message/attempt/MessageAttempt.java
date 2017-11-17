package eu.domibus.api.message.attempt;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.sql.Timestamp;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class MessageAttempt {

    private Integer id;

    /**
     * Id of the message for which the delivery attempt has been performed.
     */
    private String messageId;

    /**
     * When the attempt has started
     */
    private Timestamp startDate;

    /**
     * When the attempt has finished
     */
    private Timestamp endDate;

    private MessageAttemptStatus status;

    /**
     * Cause of the failing attempt.
     * It is null whenever the attempt has succeeded.
     */
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public MessageAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(MessageAttemptStatus status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAttempt that = (MessageAttempt) o;

        return new EqualsBuilder()
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
                .append(messageId)
                .append(startDate)
                .append(endDate)
                .append(status)
                .append(error)
                .toHashCode();
    }
}
