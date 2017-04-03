package eu.domibus.ext.delegate.converter;

import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Component
public class DomibusDomainDefaultConverter implements DomibusDomainConverter {

    @Autowired
    Mapper mapper;

    @Override
    public MessageAcknowledgementDTO convert(MessageAcknowledgement message) {
        return mapper.map(message, MessageAcknowledgementDTO.class);
    }

    @Override
    public List<MessageAcknowledgementDTO> convert(List<MessageAcknowledgement> messagesList) {
        if (messagesList == null) {
            return null;
        }
        List<MessageAcknowledgementDTO> result = new ArrayList<>();
        for (MessageAcknowledgement messageAcknowledgement : messagesList) {
            result.add(convert(messageAcknowledgement));

        }
        return result;
    }
}
