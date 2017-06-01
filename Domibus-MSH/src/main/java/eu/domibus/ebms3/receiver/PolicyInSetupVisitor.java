package eu.domibus.ebms3.receiver;

import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by dussath on 5/31/17.
 */
@Component
public class PolicyInSetupVisitor {
    @Autowired
    private PModeProvider pModeProvider;
    @Autowired
    private MessageExchangeService messageExchangeService;

    void visit(UserMessagePolicyInSetup userMessagePolicyInSetup){
        userMessagePolicyInSetup.setpModeProvider(pModeProvider);
    }
    void visit(PullRequestMessagePolicyInSetup signalMessagePolicyInSetup){
        signalMessagePolicyInSetup.setMessageExchangeService(messageExchangeService);
    }
}
