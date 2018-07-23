
package eu.domibus.common.services.impl;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class MessageIdGenerator {
    private static final String MESSAGE_ID_SUFFIX_PROPERTY = "domibus.msh.messageid.suffix";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Transactional(propagation = Propagation.SUPPORTS)
    public String generateMessageId() {
        String messageIdSuffix = domibusPropertyProvider.getDomainProperty(MESSAGE_ID_SUFFIX_PROPERTY);
        return UUID.randomUUID() + "@" + messageIdSuffix;
    }
}
