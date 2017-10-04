package eu.domibus.ebms3.common.matcher;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullReceiptMatcher")
public class PullReceiptMatcher implements ReliabilityMatcher {
    @Override
    public boolean matchReliableCallBack(LegConfiguration legConfiguration) {
        return false;
    }

    @Override
    public boolean matchReliableReceipt(LegConfiguration legConfiguration) {
        return legConfiguration.getReliability() != null && (ReplyPattern.RESPONSE.equals(legConfiguration.getReliability().getReplyPattern())
                || ReplyPattern.CALLBACK.equals(legConfiguration.getReliability().getReplyPattern()));
    }

    @Override
    public ReliabilityChecker.CheckResult fails() {
        return ReliabilityChecker.CheckResult.PULL_FAILED;
    }
}
