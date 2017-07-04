package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("serverInMessagePolicyFactory")
public class ServerInMessagePolicyFactory extends AbstractMessagePolicyFactory {
    @Override
    public MessagePolicyInSetup getMessagePolicyInSetup(SoapMessage soapMessage, Messaging messaging) {
        MessagePolicyInSetup messagePolicyInSetup = getUserMessagePolicyInSetup(soapMessage, messaging);
        if (messagePolicyInSetup == null) {
            if (messaging.getSignalMessage() != null) {
                PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
                if (pullRequest != null) {
                    PullRequestMessagePolicyInSetup policyInSetup = new PullRequestMessagePolicyInSetup(soapMessage, messaging);
                    policyInSetup.accept(getPolicyInSetupVisitor());
                    messagePolicyInSetup = policyInSetup;
                } else if (messaging.getSignalMessage().getReceipt() != null) {
                    ReceiptMessagePolicyInSetup receiptMessagePolicyInSetup = new ReceiptMessagePolicyInSetup(soapMessage, messaging);
                    receiptMessagePolicyInSetup.accept(getPolicyInSetupVisitor());
                    messagePolicyInSetup = receiptMessagePolicyInSetup;
                }
            }
        }
        return messagePolicyInSetup;
    }
}
