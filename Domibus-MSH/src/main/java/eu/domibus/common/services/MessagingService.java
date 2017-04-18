package eu.domibus.common.services;


import eu.domibus.common.exception.CompressionException;
import eu.domibus.configuration.Storage;
import eu.domibus.api.message.ebms3.model.Messaging;


/**
 * @author Ioana Dragusanu
 * @since 3.3
 */
public interface MessagingService {
    void storeMessage(Messaging messaging) throws CompressionException;

    void setStorage(Storage storage);
}
