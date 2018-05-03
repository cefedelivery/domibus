package eu.domibus.ebms3.common.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

import static eu.domibus.ebms3.common.model.MessageState.READY;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Entity
@Table(name = "TB_MESSAGING_LOCK")
@NamedQueries({
        @NamedQuery(name = "MessagingLock.findNexMessageToProcess",
                query = "select m from MessagingLock m where m.messageState=:MESSAGE_STATE and m.messageType=:MESSAGE_TYPE and m.initiator=:INITIATOR and m.mpc=:MPC ORDER BY m.received"),
        @NamedQuery(name = "MessagingLock.findNexMessageToProcessExcludingLocked",
                query = "select m from MessagingLock m where m.messageState=:MESSAGE_STATE and m.messageType=:MESSAGE_TYPE and m.initiator=:INITIATOR and m.mpc=:MPC and m.entityId not in(:LOCKED_IDS) ORDER BY m.received"),
        @NamedQuery(name = "MessagingLock.delete",
                query = "delete from MessagingLock m where m.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessagingLock.updateStatus",
                query = "update MessagingLock m set m.messageState=:MESSAGE_STATE where m.messageId=:MESSAGE_ID")
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

    public MessageState getMessageState() {
        return messageState;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getMpc() {
        return mpc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessagingLock that = (MessagingLock) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageType, that.messageType)
                .append(received, that.received)
                .append(messageState, that.messageState)
                .append(messageId, that.messageId)
                .append(initiator, that.initiator)
                .append(mpc, that.mpc)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageType)
                .append(received)
                .append(messageState)
                .append(messageId)
                .append(initiator)
                .append(mpc)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "MessagingLock{" +
                "messageType='" + messageType + '\'' +
                ", received=" + received +
                ", messageState=" + messageState +
                ", messageId='" + messageId + '\'' +
                ", initiator='" + initiator + '\'' +
                ", mpc='" + mpc + '\'' +
                '}';
    }
}
