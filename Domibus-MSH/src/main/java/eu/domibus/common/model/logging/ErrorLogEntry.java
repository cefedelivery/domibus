package eu.domibus.common.model.logging;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;
import eu.domibus.api.message.ebms3.model.Error;
import eu.domibus.api.message.ebms3.model.Messaging;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * An entry in the error log
 *
 * @author Christian Koch, Stefan Mueller
 */
@Entity
@Table(name = "TB_ERROR_LOG")
@NamedQueries({
        @NamedQuery(name = "ErrorLogEntry.findUnnotifiedErrorsByMessageId", query = "select e from ErrorLogEntry e where e.messageInErrorId = :MESSAGE_ID and e.notified is null"),
        @NamedQuery(name = "ErrorLogEntry.findErrorsByMessageId", query = "select e from ErrorLogEntry e where e.messageInErrorId = :MESSAGE_ID"),
        @NamedQuery(name = "ErrorLogEntry.findEntries", query = "select e from ErrorLogEntry e"),
        @NamedQuery(name = "ErrorLogEntry.countEntries", query = "select count(e.entityId)  from ErrorLogEntry e")
})
public class ErrorLogEntry extends AbstractBaseEntity implements ErrorResult {
    @Column(name = "ERROR_SIGNAL_MESSAGE_ID")
    private String errorSignalMessageId;
    @Enumerated(EnumType.STRING)
    @Column(name = "MSH_ROLE")
    private MSHRole mshRole;
    @Column(name = "MESSAGE_IN_ERROR_ID")
    private String messageInErrorId;
    @Enumerated(EnumType.STRING)
    @Column(name = "ERROR_CODE")
    private ErrorCode errorCode;
    @Column(name = "ERROR_DETAIL")
    private String errorDetail;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TIME_STAMP")
    private Date timestamp;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NOTIFIED")
    private Date notified;
    public ErrorLogEntry() {
    }
    /**
     * @param ebms3Exception The Exception to be logged
     */
    public ErrorLogEntry(final EbMS3Exception ebms3Exception) {
        this.mshRole = ebms3Exception.getMshRole();
        this.messageInErrorId = ebms3Exception.getRefToMessageId();
        this.errorSignalMessageId = ebms3Exception.getSignalMessageId();
        this.errorCode = ebms3Exception.getErrorCodeObject();
        this.errorDetail = ebms3Exception.getErrorDetail();
        this.timestamp = new Date();
    }

    /**
     * Creates an ErrorLogEntry from an ebMS3 signal message
     *
     * @param messaging Signal message containing the error
     * @param role      Role of the MSH
     * @return
     */
    public static ErrorLogEntry parse(final Messaging messaging, final MSHRole role) {
        final Error error = messaging.getSignalMessage().getError().iterator().next();

        final ErrorLogEntry errorLogEntry = new ErrorLogEntry();
        errorLogEntry.setTimestamp(messaging.getSignalMessage().getMessageInfo().getTimestamp());
        errorLogEntry.setErrorSignalMessageId(messaging.getSignalMessage().getMessageInfo().getMessageId());
        errorLogEntry.setErrorCode(ErrorCode.findBy(error.getErrorCode()));
        errorLogEntry.setMshRole(role);
        errorLogEntry.setMessageInErrorId(error.getRefToMessageInError());
        errorLogEntry.setErrorDetail(error.getErrorDetail());

        return errorLogEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ErrorLogEntry that = (ErrorLogEntry) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(errorSignalMessageId, that.errorSignalMessageId)
                .append(mshRole, that.mshRole)
                .append(messageInErrorId, that.messageInErrorId)
                .append(errorCode, that.errorCode)
                .append(errorDetail, that.errorDetail)
                .append(timestamp, that.timestamp)
                .append(notified, that.notified)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(errorSignalMessageId)
                .append(mshRole)
                .append(messageInErrorId)
                .append(errorCode)
                .append(errorDetail)
                .append(timestamp)
                .append(notified)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("errorSignalMessageId", errorSignalMessageId)
                .append("mshRole", mshRole)
                .append("messageInErrorId", messageInErrorId)
                .append("errorCode", errorCode)
                .append("errorDetail", errorDetail)
                .append("timestamp", timestamp)
                .append("notified", notified)
                .toString();
    }

    public String getErrorSignalMessageId() {
        return this.errorSignalMessageId;
    }

    public void setErrorSignalMessageId(final String messageId) {
        this.errorSignalMessageId = messageId;
    }

    @Override
    public MSHRole getMshRole() {
        return this.mshRole;
    }

    public void setMshRole(final MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    @Override
    public String getMessageInErrorId() {
        return this.messageInErrorId;
    }

    public void setMessageInErrorId(final String refToMessageId) {
        this.messageInErrorId = refToMessageId;
    }

    @Override
    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(final ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorDetail() {
        return this.errorDetail;
    }

    public void setErrorDetail(final String errorDetail) {
        this.errorDetail = errorDetail;
    }

    @Override
    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Date getNotified() {
        return this.notified;
    }

    public void setNotified(final Date notified) {
        this.notified = notified;
    }
}
