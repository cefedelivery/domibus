package eu.domibus.ebms3.receiver.handler;

import eu.domibus.ebms3.common.model.Messaging;
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
public class IncomingMessageHandlerDefaultFactory implements IncomingMessageHandlerFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingMessageHandlerDefaultFactory.class);

    @Autowired
    protected IncomingPullRequestHandler incomingMessagePullRequestHandler;

    @Autowired
    protected IncomingPullReceiptHandler incomingMessagePullReceiptHandler;

    @Autowired
    protected IncomingUserMessageHandler incomingUserMessageHandler;

    @Override
    public IncomingMessageHandler getMessageHandler(SOAPMessage request, Messaging messaging) {
        if (messaging.getSignalMessage() != null) {
            if (messaging.getSignalMessage().getPullRequest() != null) {
                LOG.trace("Using incomingMessagePullRequestHandler");
                return incomingMessagePullRequestHandler;
            } else if (messaging.getSignalMessage().getReceipt() != null) {
                LOG.trace("Using incomingMessagePullReceiptHandler");
                return incomingMessagePullReceiptHandler;
            } else {
                LOG.warn("No incoming message handler found");
                return null;
            }
        } else {
            LOG.trace("Using incomingUserMessageHandler");
            return incomingUserMessageHandler;
        }
    }
}
