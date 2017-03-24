package eu.domibus.ext.delegate;

import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;

import java.util.List;

/**
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
public interface DomibusDomainConverter {

   MessageAcknowledgementDTO convert(MessageAcknowledgement message);

    List<MessageAcknowledgementDTO> convert(List<MessageAcknowledgement> messagesList);
}
