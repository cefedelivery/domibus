package eu.domibus.core.nonrepudiation;

import eu.domibus.api.nonrepudiation.RawEnvelopeService;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class RawEnvelopeDefaultService implements RawEnvelopeService {

    @Autowired
    RawEnvelopeLogDao rawEnvelopeLogDao;


    @Override
    public String getRawXmlByMessageId(String messageId) {
        final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(messageId);
        if (rawXmlByMessageId != null) {
            return rawXmlByMessageId.getRawMessage();
        }
        return null;
    }
}
