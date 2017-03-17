package eu.domibus.ext.delegate;

import eu.domibus.api.acknowledge.MessageAcknowledge;
import eu.domibus.ext.MessageAcknowledgeDTO;

import java.util.List;

/**
 * Created by migueti on 15/03/2017.
 */
public class DomibusDomainDefaultConverter implements IDomibusDomainConverter {

    @Override
    public List<MessageAcknowledgeDTO> convert(List<MessageAcknowledge> messagesList) {
        return null;
    }
}
