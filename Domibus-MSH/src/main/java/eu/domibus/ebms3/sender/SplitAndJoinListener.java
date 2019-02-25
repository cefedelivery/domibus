package eu.domibus.ebms3.sender;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.impl.AS4ReceiptService;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.receiver.handler.IncomingSourceMessageHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.PolicyService;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service(value = "splitAndJoinListener")
public class SplitAndJoinListener implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinListener.class);

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

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected AS4ReceiptService as4ReceiptService;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected MSHDispatcher mshDispatcher;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        try {
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

            String messageType = message.getStringProperty(UserMessageService.MSG_TYPE);
            LOG.debug("Processing splitAndJoin message [{}]", messageType);

            if (StringUtils.equals(messageType, UserMessageService.MSG_SOURCE_MESSAGE_REJOIN)) {
                final String groupId = message.getStringProperty(UserMessageService.MSG_GROUP_ID);
                final SOAPMessage request = splitAndJoinService.rejoinSourceMessage(groupId);
                Messaging messaging = messageUtil.getMessage(request);

                MessageExchangeConfiguration userMessageExchangeContext = null;
                try {
                    userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(messaging.getUserMessage(), MSHRole.RECEIVING);
                    String pmodeKey = userMessageExchangeContext.getPmodeKey();
                    request.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
                } catch (EbMS3Exception | SOAPException e) {
                    //TODO return a signal error to C2 and notify the backend EDELIVERY-4089
                    LOG.error("Error getting the pmodeKey");
                    return;
                }

                incomingSourceMessageHandler.processMessage(request, messaging);
                userMessageService.scheduleSourceMessageReceipt(messaging.getUserMessage().getMessageInfo().getMessageId(), userMessageExchangeContext.getReversePmodeKey());
            } else if (StringUtils.equals(messageType, UserMessageService.MSG_SOURCE_MESSAGE_RECEIPT)) {
                final String sourceMessageId = message.getStringProperty(UserMessageService.MSG_SOURCE_MESSAGE_ID);
                final String pModeKey = message.getStringProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
                final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
                final Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
                final Policy policy = policyService.getPolicy(legConfiguration);
                SOAPMessage receiptMessage = null;
                try {
                    receiptMessage = as4ReceiptService.generateReceipt(sourceMessageId, false);
                } catch (EbMS3Exception e) {
                    LOG.error("Error generating the source message receipt", e);
                }
                try {
                    mshDispatcher.dispatch(receiptMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
                } catch (EbMS3Exception e) {
                    LOG.error("Error dispatching SourceMessage receipt", e);
                    throw new SplitAndJoinException("Error dispatching SourceMessage receipt", e);
                }
            } else {
                LOG.error("Unrecognized message type [{}]", messageType);
            }
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }

    }
}
