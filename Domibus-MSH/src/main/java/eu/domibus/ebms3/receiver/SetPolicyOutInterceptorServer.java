package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.SetPolicyOutInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;

/**
 * @author Thomas Dussart
 * @since 3.3
 * In case of a pulled message, the outgoing is a user message with attachements which should
 * received the same processing as the outPut of a push message.
 */
public class SetPolicyOutInterceptorServer extends SetPolicyOutInterceptor {
    public SetPolicyOutInterceptorServer() {
        super();
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        Object messageType = message.getExchange().get(MSHDispatcher.MESSAGE_TYPE_OUT);
        if(MessageType.USER_MESSAGE.equals(messageType)){
            super.handleMessage(message);
        }
    }
}
