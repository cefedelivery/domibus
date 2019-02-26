package eu.domibus.ebms3.common.matcher;

import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.ebms3.sender.ReliabilityChecker;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ReliabilityMatcher {

    boolean matchReliableCallBack(final Reliability reliability);

    boolean matchReliableReceipt(final Reliability reliability);

    ReliabilityChecker.CheckResult fails();


}
