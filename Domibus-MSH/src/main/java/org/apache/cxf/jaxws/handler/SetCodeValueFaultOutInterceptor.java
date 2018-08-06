package org.apache.cxf.jaxws.handler;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.handler.soap.DomibusSOAPHandlerFaultOutInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerFaultOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import java.util.Iterator;


/**
 * Created by idragusa on 5/26/16.
 * <p>
 * [EDELIVERY-1117]
 * The scope of this interceptor is to replace the existing CXF SOAPHandlerFaultOutInterceptor with our
 * own DomibusSOAPHandlerFaultOutInterceptor that sets the code value to Receiver instead of HandleFault
 * which is non-standard and causes an exception.
 */
public class SetCodeValueFaultOutInterceptor extends AbstractSoapInterceptor {
    public SetCodeValueFaultOutInterceptor() {
        super(Phase.PRE_PROTOCOL_FRONTEND);
        getBefore().add(SOAPHandlerFaultOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) {
        if (message == null ||
                message.getInterceptorChain() == null ||
                message.getInterceptorChain().iterator() == null)
            return;

        Iterator<Interceptor<? extends Message>> it = message.getInterceptorChain().iterator();
        while (it.hasNext()) {
            Interceptor interceptor = it.next();
            if (interceptor instanceof SOAPHandlerFaultOutInterceptor) {
                message.getInterceptorChain().add(new DomibusSOAPHandlerFaultOutInterceptor(((SOAPHandlerFaultOutInterceptor) interceptor).getBinding()));
                message.getInterceptorChain().remove(interceptor);
            }
        }
    }
}
