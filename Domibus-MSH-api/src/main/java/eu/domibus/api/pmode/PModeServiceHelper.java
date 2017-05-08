package eu.domibus.api.pmode;

import eu.domibus.api.pmode.domain.LegConfiguration;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface PModeServiceHelper {

    Integer getMaxAttempts(LegConfiguration legConfiguration);
}
