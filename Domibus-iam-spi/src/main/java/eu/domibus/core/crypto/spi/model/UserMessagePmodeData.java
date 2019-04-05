package eu.domibus.core.crypto.spi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class UserMessagePmodeData {

    private static final Logger LOG = LoggerFactory.getLogger(UserMessagePmodeData.class);

    private final String serviceName;

    private final String actionName;

    private final String partyName;

    public UserMessagePmodeData(String serviceName, String actionName, String partyName) {
        this.serviceName = serviceName;
        this.actionName = actionName;
        this.partyName = partyName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getActionName() {
        return actionName;
    }

    public String getPartyName() {
        return partyName;
    }
}
