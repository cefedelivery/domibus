package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageFilterResultRO implements Serializable {

    private List<MessageFilterRO> messageFilterEntries;

    public List<MessageFilterRO> getMessageFilterEntries() {
        return messageFilterEntries;
    }

    public void setMessageFilterEntries(List<MessageFilterRO> messageFilterEntries) {
        this.messageFilterEntries = messageFilterEntries;
    }
}
