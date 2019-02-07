package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.services.MessageExtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Sebastian-Ion TINCU
 */
@Service
public class MessageServiceDelegate implements MessageExtService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String cleanMessageIdentifier(String messageId) {
        return StringUtils.stripToEmpty(StringUtils.trimToEmpty(messageId));
    }
}
