package eu.domibus.ext.impl.v2.converter;

import eu.domibus.api.domain.MessageAcknowledge;
import eu.domibus.ext.api.v2.domain.MessageAcknowledgeExt;

import java.util.List;

/**
 * @author baciu
 */
public interface DomibusDomainConverter {

    List<MessageAcknowledgeExt> convert(List<MessageAcknowledge> messagesAcknowledged);
}
