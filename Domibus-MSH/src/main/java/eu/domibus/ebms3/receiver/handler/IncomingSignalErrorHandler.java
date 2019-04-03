package eu.domibus.ebms3.receiver.handler;

import eu.domibus.common.dao.MessagingDao;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * Handles the incoming AS4 Signal containing an error
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingSignalErrorHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingSignalErrorHandler.class);

    @Autowired
    protected MessagingDao messagingDao;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected SoapUtil soapUtil;

    @Override
    public SOAPMessage processMessage(SOAPMessage request, Messaging messaging) {
        final SignalMessage signalMessage = messaging.getSignalMessage();
        if (CollectionUtils.isEmpty(signalMessage.getError())) {
            LOG.warn("Could not process the Signal: no errors found");
            return null;
        }

        if(signalMessage.getError().size() > 1) {
            LOG.warn("More than one error received in the SignalMessage, only the first one will be processed");
        }

        final Error error = signalMessage.getError().iterator().next();
        LOG.debug("Processing Signal with error [{}]", error);

        final String refToMessageId = signalMessage.getMessageInfo().getRefToMessageId();
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(refToMessageId);
        if (userMessage == null) {
            LOG.warn("Could not process the Signal: no message [{}] found", refToMessageId);
            return null;
        }

        if (userMessage.isSourceMessage()) {
            processSourceMessageSignalError(userMessage, error);
        } else {
            LOG.warn("Could not process the Signal for message [{}]: not yet supported", refToMessageId);
        }

        LOG.debug("Finished processing Signal error");
        return null;
    }

    protected void processSourceMessageSignalError(final UserMessage sourceMessage, Error error) {
        final String messageId = sourceMessage.getMessageInfo().getMessageId();
        LOG.info("Processing Signal error for SourceMessage [{}]", messageId);

        splitAndJoinService.handleSourceMessageSignalError(messageId, error);

        LOG.debug("Finished processing Signal error for SourceMessage [{}]", messageId);
    }


}
