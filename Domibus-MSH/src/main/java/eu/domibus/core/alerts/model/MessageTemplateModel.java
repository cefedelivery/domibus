package eu.domibus.core.alerts.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MessageTemplateModel {

    private final static Logger LOG = LoggerFactory.getLogger(MessageTemplateModel.class);

    private final String messageId;

    private final String fromParty;

    private final String toParty;

    private final String oldStatus;

    private final String newStatus;

    private final String description;

    private Date reportingTime;

    public MessageTemplateModel(final String messageId,
                                final String fromParty,
                                final String toParty,
                                final String oldStatus,
                                final String newStatus,
                                final String description,
                                final Date reportingTime) {
        this.messageId = messageId;
        this.fromParty = fromParty;
        this.toParty = toParty;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.description = description;
        this.reportingTime=reportingTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getFromParty() {
        return fromParty;
    }

    public String getToParty() {
        return toParty;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getDescription() {
        return description;
    }

    public Date getReportingTime() {
        return reportingTime;
    }
}
