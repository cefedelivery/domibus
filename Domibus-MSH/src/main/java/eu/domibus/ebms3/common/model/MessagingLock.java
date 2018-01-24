package eu.domibus.ebms3.common.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static eu.domibus.ebms3.common.model.MessageState.READY;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_MESSAGING_LOCK")
@NamedQueries({
        @NamedQuery(name = "MessagingLock.findNexMessageToProcess",
                query = "select m From MessagingLock m where m.received=(select min(m.received) from MessagingLock m where m.messageState=:MESSAGE_STATE and m.messageType=:MESSAGE_TYPE and m.initiator=:INITIATOR and m.mpc=:MPC)"),
        @NamedQuery(name = "MessagingLock.findNexMessageToProcessExcludingLocked",
                query = "select m From MessagingLock m where m.received=(select min(m.received) from MessagingLock m where m.messageState=:MESSAGE_STATE and m.messageType=:MESSAGE_TYPE and m.initiator=:INITIATOR and m.mpc=:MPC and m.entityId not in(:LOCKED_IDS))")
})
public class MessagingLock extends AbstractBaseEntity {

    public final static String PULL="PULL";

    @Column(name = "MESSAGE_TYPE")
    @NotNull
    private String messageType;

    @Column(name = "MESSAGE_RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date received;

    @Column(name = "MESSAGE_STATE")
    @Enumerated(EnumType.STRING)
    private MessageState messageState;

    @Column(name = "MESSAGE_ID")
    @NotNull
    private String messageId;

    @Column(name = "INITIATOR")
    @NotNull
    private String initiator;

    @Column(name = "MPC")
    @NotNull
    private String mpc;


    public MessagingLock(final String messageId,final String initiator,final String mpc) {
        this.received = new Date();
        this.messageId = messageId;
        this.initiator=initiator;
        this.mpc=mpc;
        this.messageType=PULL;
        this.messageState=READY;
    }

    public MessagingLock() {
    }

    public String getMessageType() {
        return messageType;
    }

    public Date getReceived() {
        return received;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }
}
