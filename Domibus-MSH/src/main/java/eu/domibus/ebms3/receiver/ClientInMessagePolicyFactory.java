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
@Qualifier("clientInMessagePolicyFactory")
public class ClientInMessagePolicyFactory extends AbstractMessagePolicyFactory {

    @Override
    public MessagePolicyInSetup getMessagePolicyInSetup(SoapMessage soapMessage, Messaging messaging) {
        return getUserMessagePolicyInSetup(soapMessage, messaging);
    }
}
