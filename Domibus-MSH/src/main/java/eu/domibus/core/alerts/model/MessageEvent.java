package eu.domibus.core.alerts.model;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MessageEvent {

    private final static Logger LOG = LoggerFactory.getLogger(MessageEvent.class);

    private Date reportingTime;

    private final String messageId;

    private final MessageStatus oldStatus;

    private final MessageStatus newStatus;

    private String fromParty;

    private String toParty;

    private List<String> descriptions=new ArrayList<>();

    private final MSHRole mshRole;

    public MessageEvent(final Date reportingTime,
                        final String messageId,
                        final MessageStatus oldStatus,
                        final MessageStatus newStatus,
                        final MSHRole mshRole) {
        this.reportingTime=reportingTime;
        this.messageId = messageId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.mshRole=mshRole;
    }

    @EventReportingTime
    public Date getReportingTime() {
        return reportingTime;
    }

    @EventProperty
    public String getMessageId() {
        return messageId;
    }

    @EventProperty
    public MessageStatus getOldStatus() {
        return oldStatus;
    }

    @EventProperty
    public MessageStatus getNewStatus() {
        return newStatus;
    }

    @EventProperty
    public String getFromParty() {
        return fromParty;
    }

    @EventProperty
    public String getToParty() {
        return toParty;
    }

    @EventPropertyList
    public List<String> getDescription() {
        return Collections.unmodifiableList(descriptions);
    }

    public void addDescription(final String description){
        descriptions.add(description);
    }


    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    public void setFromParty(String fromParty) {
        this.fromParty = fromParty;
    }

    public void setToParty(String toParty) {
        this.toParty = toParty;
    }


}
