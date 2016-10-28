package eu.domibus.common.services;

//import eu.domibus.ebms3.common.model.Messaging;


import eu.domibus.common.exception.CompressionException;
import eu.domibus.ebms3.common.model.Messaging;

/**
 * Created by idragusa on 10/26/16.
 */
public interface MessagingService {

    void storeMessage(Messaging messaging) throws CompressionException;
}
