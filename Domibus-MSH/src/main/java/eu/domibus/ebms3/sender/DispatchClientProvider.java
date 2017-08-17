package eu.domibus.ebms3.sender;

import org.apache.neethi.Policy;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DispatchClientProvider {

    Dispatch<SOAPMessage> getClient(String endpoint, String algorithm, Policy policy, final String pModeKey, boolean cacheable);
}
