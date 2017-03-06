package eu.domibus.web.rest.ro;

import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class ErrorLogRO {

    private String errorSignalMessageId;
    private String mshRole;
    private String messageInErrorId;
    private String errorCode;
    private String errorDetail;
    private Date timestamp;
    private Date notified;

    public String getErrorSignalMessageId() {
        return errorSignalMessageId;
    }

    public void setErrorSignalMessageId(String errorSignalMessageId) {
        this.errorSignalMessageId = errorSignalMessageId;
    }

    public String getMshRole() {
        return mshRole;
    }

    public void setMshRole(String mshRole) {
        this.mshRole = mshRole;
    }

    public String getMessageInErrorId() {
        return messageInErrorId;
    }

    public void setMessageInErrorId(String messageInErrorId) {
        this.messageInErrorId = messageInErrorId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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
}
