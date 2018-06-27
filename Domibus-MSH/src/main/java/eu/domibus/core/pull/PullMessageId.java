package eu.domibus.core.pull;

import static eu.domibus.core.pull.PullMessageState.FIRST_ATTEMPT;

public class PullMessageId {

    private String messageId;

    private PullMessageState state;

    private String staledReason;

    public PullMessageId(final String messageId, final PullMessageState state, final String staleReason) {
        this.messageId = messageId;
        this.state = state;
        this.staledReason =staleReason;
    }

    public PullMessageId(final String messageId, final PullMessageState state) {
        this(messageId, state, null);
    }

    public PullMessageId(final String messageId) {
        this(messageId, FIRST_ATTEMPT, null);
    }

    public String getMessageId() {
        return messageId;
    }

    public PullMessageState getState() {
        return state;
    }

    public String getStaledReason() {
        return staledReason;
    }
}
