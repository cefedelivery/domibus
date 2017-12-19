package eu.domibus.common.services;


import eu.domibus.common.MSHRole;
import eu.domibus.configuration.Storage;
import eu.domibus.ebms3.common.model.Messaging;


/**
 * @author Ioana Dragusanu
 * @since 3.3
 */
public interface MessagingService {
    void storeMessage(Messaging messaging, MSHRole mshRole);

    void setStorage(Storage storage);
}
