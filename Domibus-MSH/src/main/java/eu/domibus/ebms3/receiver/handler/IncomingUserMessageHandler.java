package eu.domibus.ebms3.receiver.handler;

import eu.domibus.api.message.UserMessageException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartyInfo;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.receiver.UserMessageHandlerContext;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceException;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingUserMessageHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingUserMessageHandler.class);

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private UserMessageHandlerService userMessageHandlerService;

    @Autowired
    protected MessageUtil messageUtil;

    @Override
    public SOAPMessage processMessage(SOAPMessage request, Messaging messaging) {
        SOAPMessage responseMessage = null;
        String pmodeKey = null;
        try {
            pmodeKey = (String) request.getProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
        } catch (final SOAPException soapEx) {
            //this error should never occur because pmode handling is done inside the in-interceptorchain
            LOG.error("Cannot find PModeKey property for incoming Message", soapEx);
            assert false;
        }
        UserMessageHandlerContext userMessageHandlerContext = getMessageHandler();
        try {
            LOG.info("Using pmodeKey {}", pmodeKey);
            responseMessage = userMessageHandlerService.handleNewUserMessage(pmodeKey, request, messaging, userMessageHandlerContext);
            final PartyInfo partyInfo = messaging.getUserMessage().getPartyInfo();
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED, partyInfo.getFrom().getFirstPartyId(), partyInfo.getTo().getFirstPartyId());
            LOG.debug("Ping message {}", userMessageHandlerContext.isTestMessage());
        } catch (TransformerException | SOAPException | JAXBException | IOException e) {
            throw new UserMessageException(e);
        } catch (final EbMS3Exception e) {
            try {
                if (!userMessageHandlerContext.isTestMessage() && userMessageHandlerContext.getLegConfiguration().getErrorHandling().isBusinessErrorNotifyConsumer()) {
                    backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(e));
                }
            } catch (Exception ex) {
                LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, userMessageHandlerContext.getMessageId());
            }
            throw new WebServiceException(e);
        }
        return responseMessage;
    }

    protected UserMessageHandlerContext getMessageHandler() {
        return new UserMessageHandlerContext();
    }


}
