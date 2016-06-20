/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.common.model.logging;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.AbstractBaseEntity;
import eu.domibus.common.model.Error;
import eu.domibus.common.model.Messaging;

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
        if (!(o instanceof ErrorLogEntry)) return false;
        if (!super.equals(o)) return false;

        ErrorLogEntry that = (ErrorLogEntry) o;

        if (errorSignalMessageId != null ? !errorSignalMessageId.equals(that.errorSignalMessageId) : that.errorSignalMessageId != null)
            return false;
        if (mshRole != that.mshRole) return false;
        if (messageInErrorId != null ? !messageInErrorId.equals(that.messageInErrorId) : that.messageInErrorId != null)
            return false;
        if (errorCode != that.errorCode) return false;
        if (errorDetail != null ? !errorDetail.equals(that.errorDetail) : that.errorDetail != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        return !(notified != null ? !notified.equals(that.notified) : that.notified != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (errorSignalMessageId != null ? errorSignalMessageId.hashCode() : 0);
        result = 31 * result + (mshRole != null ? mshRole.hashCode() : 0);
        result = 31 * result + (messageInErrorId != null ? messageInErrorId.hashCode() : 0);
        result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
        result = 31 * result + (errorDetail != null ? errorDetail.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (notified != null ? notified.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ErrorLogEntry{" +
                "mshRole=" + mshRole +
                ", messageInErrorId='" + messageInErrorId + '\'' +
                ", errorCode=" + errorCode +
                ", errorDetail='" + errorDetail + '\'' +
                ", timestamp=" + timestamp +
                '}';
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
