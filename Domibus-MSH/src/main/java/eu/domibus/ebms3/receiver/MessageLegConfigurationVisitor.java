package eu.domibus.ebms3.receiver;

import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Component
 class MessageLegConfigurationVisitor {
    @Autowired
    private PModeProvider pModeProvider;
    @Autowired
    private MessageExchangeService messageExchangeService;
    @Autowired
    private MessagingDao messagingDao;

    void visit(UserMessageLegConfigurationExtractor userMessagePolicyInSetup){
        userMessagePolicyInSetup.setpModeProvider(pModeProvider);
    }
    void visit(PullRequestLegConfigurationExtractor signalMessagePolicyInSetup){
        signalMessagePolicyInSetup.setMessageExchangeService(messageExchangeService);
    }
    void visit(ReceiptLegConfigurationExtractor receiptMessagePolicyInSetup){
        receiptMessagePolicyInSetup.setMessagingDao(messagingDao);
        receiptMessagePolicyInSetup.setpModeProvider(pModeProvider);
    }
}
