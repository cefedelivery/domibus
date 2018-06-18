package eu.domibus.core.pull;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.logging.UserMessageLog;

import java.util.Date;

public class PullRequestResult {

    private final int sendAttempts;

    private final Date nextAttempts;

    private final MessageStatus messageStatus;

    private final String messageId;

    public PullRequestResult(final UserMessageLog userMessageLog) {
        this.sendAttempts = userMessageLog.getSendAttempts();
        this.nextAttempts = userMessageLog.getNextAttempt();
        this.messageStatus = userMessageLog.getMessageStatus();
        this.messageId = userMessageLog.getMessageId();
    }


    public int getSendAttempts() {
        return sendAttempts;
    }

    public Date getNextAttempts() {
        return nextAttempts;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public String getMessageId() {
        return messageId;
    }
}
