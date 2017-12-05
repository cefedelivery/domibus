package eu.domibus.web.rest.ro;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class ErrorLogRO implements Serializable {

    private String errorSignalMessageId;
    private MSHRole mshRole;
    private String messageInErrorId;
    private ErrorCode errorCode;
    private String errorDetail;
    private Date timestamp;
    private Date notified;

    public String getErrorSignalMessageId() {
        return errorSignalMessageId;
    }

    public void setErrorSignalMessageId(String errorSignalMessageId) {
        this.errorSignalMessageId = errorSignalMessageId;
    }

    public String getMessageInErrorId() {
        return messageInErrorId;
    }

    public void setMessageInErrorId(String messageInErrorId) {
        this.messageInErrorId = messageInErrorId;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getNotified() {
        return notified;
    }

    public void setNotified(Date notified) {
        this.notified = notified;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
