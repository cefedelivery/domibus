package eu.domibus.ebms3.common.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static eu.domibus.ebms3.common.model.MessageState.READY;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Entity
@Table(name = "TB_MESSAGING_LOCK")
@NamedQueries({
        @NamedQuery(name = "MessagingLock.findForMessageId",
                query = "select m from MessagingLock m where m.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessagingLock.delete",
                query = "delete from MessagingLock m where m.messageId=:MESSAGE_ID"),
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

    @Column(name = "MESSAGE_STALED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date staled;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    public MessagingLock(
            final String messageId,
            final String initiator,
            final String mpc,
            final Date received,
            final Date staled,
            final Date nextAttempt,
            final int sendAttempts,
            final int sendAttemptsMax) {
        this.received = received;
        this.staled=staled;
        this.messageId = messageId;
        this.initiator=initiator;
        this.mpc=mpc;
        this.messageType=PULL;
        this.messageState=READY;
        this.nextAttempt=nextAttempt;
        this.sendAttempts=sendAttempts;
        this.sendAttemptsMax=sendAttemptsMax;
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
                .append(staled, that.staled)
                .append(nextAttempt, that.nextAttempt)
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
                .append(staled)
                .append(nextAttempt)
                .toHashCode();
    }
}
