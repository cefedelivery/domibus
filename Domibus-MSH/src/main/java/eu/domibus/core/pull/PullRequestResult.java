package eu.domibus.core.pull;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.logging.UserMessageLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class PullRequestResult {

    private final static Logger LOG = LoggerFactory.getLogger(PullRequestResult.class);

    private int sendAttempts;
    private Date nextAttempts;
    private MessageStatus messageStatus;
    private String messageId;

    public PullRequestResult(int sendAttempts, Date nextAttempts, MessageStatus messageStatus, String messageId) {
        this.sendAttempts = sendAttempts;
        this.nextAttempts = nextAttempts;
        this.messageStatus = messageStatus;
        this.messageId = messageId;
    }

    public PullRequestResult(UserMessageLog userMessageLog) {
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
