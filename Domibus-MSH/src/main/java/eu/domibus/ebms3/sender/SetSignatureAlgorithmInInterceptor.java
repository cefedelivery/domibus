
package eu.domibus.ebms3.sender;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.apache.cxf.ws.security.SecurityConstants;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class SetSignatureAlgorithmInInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetSignatureAlgorithmInInterceptor.class);

    public SetSignatureAlgorithmInInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(PolicyInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {

        final Object signatureAlgorithm = message.getContextualProperty(DispatchClientDefaultProvider.ASYMMETRIC_SIG_ALGO_PROPERTY);
        if (signatureAlgorithm != null) {
            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, signatureAlgorithm);
        }
    }
}
