package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

abstract class AbstractMessagePolicyFactory implements MessagePolicyFactory {
    @Autowired
    private PolicyInSetupVisitor policyInSetupVisitor;

    MessagePolicyInSetup getUserMessagePolicyInSetup(SoapMessage soapMessage, Messaging messaging){
        MessagePolicyInSetup messagePolicyInSetup=null;
        if(messaging.getUserMessage()!=null){
            UserMessagePolicyInSetup userMessagePolicyInSetup = new UserMessagePolicyInSetup(soapMessage, messaging);
            userMessagePolicyInSetup.accept(getPolicyInSetupVisitor());
            messagePolicyInSetup=userMessagePolicyInSetup;
        }
        return messagePolicyInSetup;
    }


    PolicyInSetupVisitor getPolicyInSetupVisitor() {
        return policyInSetupVisitor;
    }
}
