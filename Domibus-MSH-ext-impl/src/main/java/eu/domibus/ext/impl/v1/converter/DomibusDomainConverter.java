package eu.domibus.ext.impl.v1.converter;

import eu.domibus.api.domain.MessageAcknowledge;
import eu.domibus.ext.api.v1.domain.MessageAcknowledgeDTO;

import java.util.List;

/**
 * @author baciu
 */
public interface DomibusDomainConverter {

    List<MessageAcknowledgeDTO> convert(List<MessageAcknowledge> messagesAcknowledged);
}
