package eu.domibus.ebms3.sender;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.AS4ReceiptService;
import eu.domibus.common.services.impl.UserMessageHandlerService;
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
import javax.xml.transform.TransformerException;

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

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected MessagingService messagingService;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

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
                final String backendName = message.getStringProperty(UserMessageService.MSG_BACKEND_NAME);
                final Domain currentDomain = domainContextProvider.getCurrentDomain();
                domainTaskExecutor.submitLongRunningTask(
                        () -> {
                            LOG.debug("Saving the incoming SourceMessage payloads");

                            final SOAPMessage sourceRequest = splitAndJoinService.rejoinSourceMessage(groupId);
                            Messaging sourceMessaging = messageUtil.getMessage(sourceRequest);

                            MessageExchangeConfiguration userMessageExchangeContext = null;
                            LegConfiguration legConfiguration = null;
                            try {
                                userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(sourceMessaging.getUserMessage(), MSHRole.RECEIVING);
                                String sourcePmodeKey = userMessageExchangeContext.getPmodeKey();
                                legConfiguration = pModeProvider.getLegConfiguration(sourcePmodeKey);
                                sourceRequest.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, sourcePmodeKey);
                            } catch (EbMS3Exception | SOAPException e) {
                                //TODO return a signal error to C2 and notify the backend EDELIVERY-4089
                                LOG.error("Error getting the pmodeKey", e);
                                return;
                            }

                            try {
                                userMessageHandlerService.handlePayloads(sourceRequest, sourceMessaging.getUserMessage());
                            } catch (EbMS3Exception | SOAPException | TransformerException e) {
                                //TODO return a signal error to C2 and notify the backend EDELIVERY-4089
                                LOG.error("Error handling payloads", e);
                                return;
                            }

                            messagingService.storePayloads(sourceMessaging, MSHRole.RECEIVING, legConfiguration, backendName);

                            incomingSourceMessageHandler.processMessage(sourceRequest, sourceMessaging);
                            userMessageService.scheduleSourceMessageReceipt(sourceMessaging.getUserMessage().getMessageInfo().getMessageId(), userMessageExchangeContext.getReversePmodeKey());
                        },
                        currentDomain);
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
