package eu.domibus.ext.delegate.converter;

import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
@Component
public class DomibusDomainDefaultConverter implements DomibusDomainConverter {

    @Override
    public MessageAcknowledgementDTO convert(MessageAcknowledgement message) {
        return null;
    }

    @Override
    public List<MessageAcknowledgementDTO> convert(List<MessageAcknowledgement> messagesList) {
        return null;
    }
}
