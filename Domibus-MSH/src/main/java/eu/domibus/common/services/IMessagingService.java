package eu.domibus.common.services;

import eu.domibus.ebms3.common.model.Messaging;

import java.io.IOException;

/**
 * Created by idragusa on 10/26/16.
 */
public interface IMessagingService {

    void storeMessage(Messaging messaging) throws IOException;
}
