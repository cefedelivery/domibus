package eu.domibus.ext.delegate;

import eu.domibus.api.acknowledge.MessageAcknowledge;
import eu.domibus.ext.domain.MessageAcknowledgeDTO;

import java.util.List;

/**
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
public class DomibusDomainDefaultConverter implements DomibusDomainConverter {

    @Override
    public List<MessageAcknowledgeDTO> convert(List<MessageAcknowledge> messagesList) {
        return null;
    }
}
