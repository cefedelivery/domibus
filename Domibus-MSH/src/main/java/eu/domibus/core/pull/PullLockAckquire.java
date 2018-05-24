package eu.domibus.core.pull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullLockAckquire {

    private final static Logger LOG = LoggerFactory.getLogger(PullLockAckquire.class);

    private final long id;

    private final long expirationTimeStamp;

    public PullLockAckquire(long id, long expirationTimeStamp) {
        this.id = id;
        this.expirationTimeStamp = expirationTimeStamp;
    }

    public long getExpirationTimeStamp() {
        return expirationTimeStamp;
    }
}
