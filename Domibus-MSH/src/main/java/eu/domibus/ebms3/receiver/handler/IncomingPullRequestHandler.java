package eu.domibus.ebms3.receiver.handler;

import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingPullRequestHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullRequestHandler.class);

    @Autowired
    private PullRequestHandler pullRequestHandler;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Override
    public SOAPMessage processMessage(SOAPMessage request, Messaging messaging) {
        LOG.trace("before pull request.");
        final SOAPMessage soapMessage = handlePullRequest(messaging);
        LOG.trace("returning pull request message.");
        return soapMessage;
    }

    protected SOAPMessage handlePullRequest(Messaging messaging) {
        PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        PullContext pullContext = messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
        String messageId = messageExchangeService.retrieveReadyToPullUserMessageId(pullContext.getMpcQualifiedName(), pullContext.getInitiator());
        return pullRequestHandler.handlePullRequest(messageId, pullContext);
    }
}
