package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * DTO class for the Messages Monitor Service.
 *
 * It stores information about a delivery attempt for a certain message.
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public class MessageAttemptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageId", messageId)
                .append("startDate", startDate)
                .append("endDate", endDate)
                .append("status", status)
                .append("error", error)
                .toString();
    }
}
