package eu.domibus.web.rest.ro;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.ebms3.common.model.MessageType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageLogResultRO implements Serializable {

    private Map<String, Object> filter;
    private List<MessageLogRO> messageLogEntries;

    private MSHRole[] mshRoles;
    private MessageType[] msgTypes;
    private MessageStatus[] msgStatus;
    private NotificationStatus[] notifStatus;

    private Integer count;
    private Integer page;
    private Integer pageSize;

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    public List<MessageLogRO> getMessageLogEntries() {
        return messageLogEntries;
    }

    public void setMessageLogEntries(List<MessageLogRO> messageLogEntries) {
        this.messageLogEntries = messageLogEntries;
    }

    public MSHRole[] getMshRoles() {
        return mshRoles;
    }

    public void setMshRoles(MSHRole[] mshRoles) {
        this.mshRoles = mshRoles;
    }

    public MessageType[] getMsgTypes() {
        return msgTypes;
    }

    public void setMsgTypes(MessageType[] msgTypes) {
        this.msgTypes = msgTypes;
    }

    public MessageStatus[] getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(MessageStatus[] msgStatus) {
        this.msgStatus = msgStatus;
    }

    public NotificationStatus[] getNotifStatus() {
        return notifStatus;
    }

    public void setNotifStatus(NotificationStatus[] notifStatus) {
        this.notifStatus = notifStatus;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
