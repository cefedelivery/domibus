package eu.domibus.ebms3.sender;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyInInterceptor;

/**
 * 
 * @author Cosmin Baciu
 * @since 4.1
 */
public class DomibusPrepareAttachmentInterceptor extends AbstractSoapInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPrepareAttachmentInterceptor.class);


    public DomibusPrepareAttachmentInterceptor() {
        super(Phase.SETUP);
        this.addBefore(PolicyInInterceptor.class.getName());
    }

   
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        LOG.debug("DomibusPrepareAttachmentInterceptor");
        message.getExchange().put(ClientImpl.FINISHED, Boolean.TRUE);
        message.getInterceptorChain().add(new PrepareAttachmentInterceptor());
    }
}
