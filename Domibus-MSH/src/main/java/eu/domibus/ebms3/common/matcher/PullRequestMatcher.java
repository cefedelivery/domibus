package eu.domibus.ebms3.common.matcher;

import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullRequestMatcher")
public class PullRequestMatcher implements ReliabilityMatcher {
    @Override
    public boolean matchReliableCallBack(Reliability reliability) {
        if (reliability == null) {
            return false;
        }

        return (ReplyPattern.RESPONSE.equals(reliability.getReplyPattern())
                || ReplyPattern.CALLBACK.equals(reliability.getReplyPattern()));
    }

    @Override
    public boolean matchReliableReceipt(Reliability reliability) {
        return false;
    }

    @Override
    public ReliabilityChecker.CheckResult fails() {
        return ReliabilityChecker.CheckResult.PULL_FAILED;
    }
}
