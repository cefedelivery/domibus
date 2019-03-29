package eu.domibus.ebms3.receiver.handler;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    protected IncomingUserMessageReceiptHandler incomingUserMessageReceiptHandler;

    @Autowired
    protected IncomingSignalErrorHandler incomingSignalErrorHandler;

    @Override
    public IncomingMessageHandler getMessageHandler(SOAPMessage request, Messaging messaging) {
        final SignalMessage signalMessage = messaging.getSignalMessage();
        if (signalMessage != null) {
            if (signalMessage.getPullRequest() != null) {
                LOG.trace("Using incomingMessagePullRequestHandler");
                return incomingMessagePullRequestHandler;
            } else if (signalMessage.getReceipt() != null) {
                final String contentsOfReceipt = signalMessage.getReceipt().getAny().get(0);
                if (StringUtils.contains(contentsOfReceipt, "UserMessage")) {
                    LOG.trace("Using incomingUserMessageReceiptHandler");
                    return incomingUserMessageReceiptHandler;
                } else {
                    LOG.trace("Using incomingMessagePullReceiptHandler");
                    return incomingMessagePullReceiptHandler;
                }
            } else if (CollectionUtils.isNotEmpty(signalMessage.getError())) {
                LOG.trace("Using incomingSignalErrorHandler");
                return incomingSignalErrorHandler;
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
