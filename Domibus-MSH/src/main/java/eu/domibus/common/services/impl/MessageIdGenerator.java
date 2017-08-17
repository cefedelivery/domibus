
package eu.domibus.common.services.impl;

import java.util.UUID;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class MessageIdGenerator {

    private String messageIdSuffix;

    public String generateMessageId() {
        return UUID.randomUUID() + "@" + this.messageIdSuffix;
    }

    public void setMessageIdSuffix(final String messageIdSuffix) {
        this.messageIdSuffix = messageIdSuffix;
    }
}
