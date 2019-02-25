package eu.domibus.ebms3.sender;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import java.util.Iterator;


public class DomibusAttachmentOutInterceptorRemoval extends AbstractSoapInterceptor {
    public DomibusAttachmentOutInterceptorRemoval() {
        super(Phase.PRE_STREAM);
        super.addBefore(AttachmentOutInterceptor.class.getName());
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
            if (interceptor.getClass().getName().equals(AttachmentOutInterceptor.class.getName())) {
                message.getInterceptorChain().remove(interceptor);
                return;
            }
        }
    }
}
