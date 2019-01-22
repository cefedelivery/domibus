package eu.domibus.ebms3.sender;

import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.receiver.handler.IncomingSourceMessageHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service(value = "largeMessageSenderListener")
public class LargeMessageSenderListener extends AbstractMessageSenderListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LargeMessageSenderListener.class);

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected IncomingSourceMessageHandler incomingSourceMessageHandler;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected SoapUtil soapUtil;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        LOG.debug("Processing large message [{}]", message);

        try {
            String messageType = message.getStringProperty(UserMessageService.MSG_TYPE);
            if (StringUtils.equals(messageType, UserMessageService.MSG_SOURCE_USER_MESSAGE_REJOIN)) {
                String domainCode = null;
                try {
                    domainCode = message.getStringProperty(MessageConstants.DOMAIN);
                } catch (final JMSException e) {
                    LOG.error("Error processing JMS message", e);
                }
                if (StringUtils.isBlank(domainCode)) {
                    LOG.error("Domain is empty: could not send message");
                    return;
                }

                domainContextProvider.setCurrentDomain(domainCode);
                final String groupId = message.getStringProperty(UserMessageService.MSG_GROUP_ID);
                final SOAPMessage request = splitAndJoinService.rejoinSourceMessage(groupId);
                Messaging messaging = messageUtil.getMessage(request);
                String pmodeKey = null;
                try {
                    pmodeKey = pModeProvider.findUserMessageExchangeContext(messaging.getUserMessage(), MSHRole.RECEIVING).getPmodeKey();
                    request.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
                } catch (EbMS3Exception | SOAPException e) {
                    //TODO return a signal error to C2 and notify the backend
                    LOG.error("Error getting the pmodeKey");
                    return;
                }

                final SOAPMessage response = incomingSourceMessageHandler.processMessage(request, messaging);

                if (LOG.isDebugEnabled()) {
                    try {
                        LOG.debug("SourceMessage receipt [{}] " + soapUtil.getRawXMLMessage(response));
                    } catch (TransformerException e) {
                        LOG.debug("Could not log SourceMessage receipt", e);
                    }
                }

                //TODO send the receipt
            } else {
                super.onMessage(message);
            }
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }

    }
}
