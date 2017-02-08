package eu.domibus.ext.services.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO class for the Messages Monitor Service.
 *
 * It stores information about a delivery attempt for a certain message.
 *
 * @author Federico Martini
 * @since 3.3
 */
public class AttemptDTO implements Serializable {

    /**
     * Id of the message for which the delivery attempt has been performed.
     */
    private String messageId;

    /**
     * Progressive number of the attempt performed
     */
    private int number;

    /**
     * When the attempt has started
     */
    private Date start;

    /**
     * When the attempt has finished
     */
    private Date end;

    /**
     * Cause of the failing attempt.
     * It is null whenever the attempt has succeeded.
     */
    private String failingCause;

    public String getMessageId() {
        return messageId;
    }

    public AttemptDTO setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public int getNumber() {
        return number;
    }

    public AttemptDTO setNumber(int number) {
        this.number = number;
        return this;
    }

    public Date getStart() {
        return start;
    }

    public AttemptDTO setStart(Date start) {
        this.start = start;
        return this;
    }

    public Date getEnd() {
        return end;
    }

    public AttemptDTO setEnd(Date end) {
        this.end = end;
        return this;
    }

    public String getFailingCause() {
        return failingCause;
    }

    public AttemptDTO setFailingCause(String failingCause) {
        this.failingCause = failingCause;
        return this;
    }

}
