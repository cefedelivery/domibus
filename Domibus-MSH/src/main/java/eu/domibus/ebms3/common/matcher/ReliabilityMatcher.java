package eu.domibus.ebms3.common.matcher;

import eu.domibus.common.model.configuration.LegConfiguration;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ReliabilityMatcher {

    boolean matchReliableCallBack(final LegConfiguration legConfiguration);

    boolean matchReliableReceipt(final LegConfiguration legConfiguration);


}
