package eu.domibus.core.crypto.spi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PullRequest {

    private static final Logger LOG = LoggerFactory.getLogger(PullRequest.class);

    private String mpc;

    public String getMpc() {
        return mpc;
    }

    public void setMpc(String mpc) {
        this.mpc = mpc;
    }
}
