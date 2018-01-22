package eu.domibus.ebms3.common.model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Table(name = "TB_MESSAGING_LOCK")
@NamedQueries({
        @NamedQuery(name = "MessagingLock.findNexMessageToProcess",
                query = "MessagingLock m where m.received=(select min(m.received) from MessagingLock m where m.processed=false and m.messageType=:MESSAGE_TYPE)")
})
public class MessagingLock extends AbstractBaseEntity {

    public final static String PULL="PULL";

    private String messageType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    private Boolean processed;

    private String messageId;

    public String getMessageType() {
        return messageType;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
