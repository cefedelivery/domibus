package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullRequestLegConfigurationFactory")
public class PullRequestLegConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Messaging messaging) {
        PullRequestLegConfigurationExtractor legConfigurationExtractor=null;
        PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        if (pullRequest != null) {
            legConfigurationExtractor = new PullRequestLegConfigurationExtractor(soapMessage, messaging);
        }
        return legConfigurationExtractor;
    }
}
