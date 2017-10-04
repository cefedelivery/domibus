package eu.domibus.core.message;

import eu.domibus.ebms3.common.model.Messaging;

/**
 * Created by musatmi on 11/05/2017.
 */
public interface MessageConverterService {
    byte[] getAsByteArray(Messaging message);
}
