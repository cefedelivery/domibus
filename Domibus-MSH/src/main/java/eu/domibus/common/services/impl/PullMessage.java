package eu.domibus.common.services.impl;

/**
 * Created by dussath on 6/2/17.
 * Message used by the pull jms queue
 */
public class PullMessage {
    private final String mpc;
    private final String pmodeKey;

    public PullMessage(final String mpc, final String pmodeKey) {
        this.mpc = mpc;
        this.pmodeKey = pmodeKey;
    }

    public String getMpc() {
        return mpc;
    }

    public String getPmodeKey() {
        return pmodeKey;
    }
}
