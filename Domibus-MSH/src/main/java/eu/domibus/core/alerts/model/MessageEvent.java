package eu.domibus.core.alerts.model;

import eu.domibus.common.MessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageEvent extends Event{

    private final static Logger LOG = LoggerFactory.getLogger(MessageEvent.class);

    private final String messageId;

    private final MessageStatus oldStatus;

    private final MessageStatus newStatus;

    private String fromParty;

    private String toParty;

    private String description;

    public MessageEvent(final String messageId,
                        final MessageStatus oldStatus,
                        final MessageStatus newStatus) {

        this.messageId = messageId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public void setFromParty(String fromParty) {
        this.fromParty = fromParty;
    }

    public void setToParty(String toParty) {
        this.toParty = toParty;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
