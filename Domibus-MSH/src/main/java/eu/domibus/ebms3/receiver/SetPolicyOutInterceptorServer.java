package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.SetPolicyOutInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyInInterceptor;

/**
 * Created by dussath on 6/7/17.
 */
public class SetPolicyOutInterceptorServer extends SetPolicyOutInterceptor {
    public SetPolicyOutInterceptorServer() {
        super();
       // this.addBefore(PolicyInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        Object policy = message.getExchange().get(PolicyConstants.POLICY_OVERRIDE);
        Object messageType = message.getExchange().get(MSHDispatcher.MESSAGE_TYPE_OUT);
        System.out.println("Policy "+policy);
        System.out.println("Message type "+messageType);
        if(MessageType.USER_MESSAGE.equals(messageType)){
            super.handleMessage(message);
        }
    }
}
