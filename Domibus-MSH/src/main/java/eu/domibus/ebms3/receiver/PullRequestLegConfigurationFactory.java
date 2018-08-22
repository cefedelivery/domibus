package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullRequestLegConfigurationFactory")
public class PullRequestLegConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(PullRequestLegConfigurationFactory.class);

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Messaging messaging) {
        PullRequestLegConfigurationExtractor legConfigurationExtractor = null;
        if (LOG.isTraceEnabled()) {
            final List<Header> headers = soapMessage.getHeaders();
            if (headers != null) {
                LOG.trace("Soap message: ");
                for (Header header : headers) {
                    LOG.trace("Header [{}], object[{}]", header.getName(), header.getObject());
                }
            }
            LOG.trace("User message:[{}] ", messaging.getUserMessage());
            LOG.trace("Signal message:[{}] ", messaging.getSignalMessage());
            if (messaging.getSignalMessage() != null) {
                LOG.trace("Pull request:[{}]", messaging.getSignalMessage().getPullRequest());
                LOG.trace("Receipt:[{}]", messaging.getSignalMessage().getReceipt());
                final Set<Error> errors = messaging.getSignalMessage().getError();
                if (errors != null) {
                    for (Error error : errors) {
                        LOG.trace("Error code:[{}], detail[{}]", error.getErrorCode(), error.getErrorDetail());
                    }
                }
            }
        }
        PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        if (pullRequest != null) {
            legConfigurationExtractor = new PullRequestLegConfigurationExtractor(soapMessage, messaging);
        }
        return legConfigurationExtractor;
    }
}
