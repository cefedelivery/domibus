package eu.domibus.core.pmode;

import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class PModeDefaultServiceHelper implements PModeServiceHelper {

    @Override
    public Integer getMaxAttempts(LegConfiguration legConfiguration) {
        return legConfiguration.getReceptionAwareness() == null ? 1 : legConfiguration.getReceptionAwareness().getRetryCount();
    }
}
