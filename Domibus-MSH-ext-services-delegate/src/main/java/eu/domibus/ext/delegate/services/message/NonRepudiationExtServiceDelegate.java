package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.nonrepudiation.RawEnvelopeService;
import eu.domibus.ext.services.NonRepudiationExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class NonRepudiationExtServiceDelegate implements NonRepudiationExtService {

    @Autowired
    protected RawEnvelopeService rawEnvelopeService;

    @Override
    public String getRawXmlByMessageId(String messageId) {
        return rawEnvelopeService.getRawXmlByMessageId(messageId);
    }
}
