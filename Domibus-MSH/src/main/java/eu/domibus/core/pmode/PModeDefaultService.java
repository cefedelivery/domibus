package eu.domibus.core.pmode;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.api.pmode.domain.ReceptionAwareness;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class PModeDefaultService implements PModeService {

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Override
    public LegConfiguration getLegConfiguration(String messageId) {
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        String pModeKey = null;
        try {
            pModeKey = pModeProvider.findPModeKeyForUserMessage(userMessage, MSHRole.SENDING);
        } catch (EbMS3Exception e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not get the PMode key for message [" + messageId + "]", e);
        }
        eu.domibus.common.model.configuration.LegConfiguration legConfigurationEntity = pModeProvider.getLegConfiguration(pModeKey);
        return convert(legConfigurationEntity);
    }

    protected LegConfiguration convert(eu.domibus.common.model.configuration.LegConfiguration legConfigurationEntity) {
        if (legConfigurationEntity == null) {
            return null;
        }
        final LegConfiguration result = new LegConfiguration();
        result.setReceptionAwareness(convert(legConfigurationEntity.getReceptionAwareness()));
        return result;
    }

    protected ReceptionAwareness convert(eu.domibus.common.model.configuration.ReceptionAwareness receptionAwarenessEntity) {
        if (receptionAwarenessEntity == null) {
            return null;
        }
        ReceptionAwareness result = new ReceptionAwareness();
        result.setDuplicateDetection(receptionAwarenessEntity.getDuplicateDetection());
        result.setName(receptionAwarenessEntity.getName());
        result.setRetryCount(receptionAwarenessEntity.getRetryCount());
        result.setRetryTimeout(receptionAwarenessEntity.getRetryTimeout());
        return result;
    }
}
