package eu.domibus.ext.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PullRequestDTO {

    private static final Logger LOG = LoggerFactory.getLogger(PullRequestDTO.class);

    private String mpc;

    public String getMpc() {
        return mpc;
    }

    public void setMpc(String mpc) {
        this.mpc = mpc;
    }
}
