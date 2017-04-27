package eu.domibus.api.message.attempt;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class MessageAttempt {

    /**
     * Id of the message for which the delivery attempt has been performed.
     */
    private String messageId;

    /**
     * When the attempt has started
     */
    private Date start;

    /**
     * When the attempt has finished
     */
    private Date end;

    private MessageAttemptStatus status;

    /**
     * Cause of the failing attempt.
     * It is null whenever the attempt has succeeded.
     */
    private String failingCause;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getFailingCause() {
        return failingCause;
    }

    public void setFailingCause(String failingCause) {
        this.failingCause = failingCause;
    }

    public MessageAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(MessageAttemptStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAttempt that = (MessageAttempt) o;

        return new EqualsBuilder()
                .append(messageId, that.messageId)
                .append(start, that.start)
                .append(end, that.end)
                .append(status, that.status)
                .append(failingCause, that.failingCause)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageId)
                .append(start)
                .append(end)
                .append(status)
                .append(failingCause)
                .toHashCode();
    }
}
