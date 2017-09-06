
package eu.domibus.common.services.impl;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class MessageIdGenerator {

    private String messageIdSuffix;

    @Transactional(propagation = Propagation.SUPPORTS)
    public String generateMessageId() {
        return UUID.randomUUID() + "@" + this.messageIdSuffix;
    }

    public void setMessageIdSuffix(final String messageIdSuffix) {
        this.messageIdSuffix = messageIdSuffix;
    }
}
