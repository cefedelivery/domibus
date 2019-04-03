package eu.domibus.core.crypto.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PullRequestPmodeData {

    private static final Logger LOG = LoggerFactory.getLogger(PullRequestPmodeData.class);

    private final String mpcName;

    public PullRequestPmodeData(String mpcName) {
        this.mpcName = mpcName;
    }

    public String getMpcName() {
        return mpcName;
    }
}
