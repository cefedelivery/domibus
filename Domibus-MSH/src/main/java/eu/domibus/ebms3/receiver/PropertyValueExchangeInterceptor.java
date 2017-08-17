
package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * This interceptor is responsible for the exchange of parameters from a org.apache.cxf.binding.soap.SoapMessage to a javax.xml.soap.SOAPException
 *
 * @author Christian Koch, Stefan Mueller
 */
public class PropertyValueExchangeInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyValueExchangeInterceptor.class);

    public PropertyValueExchangeInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {

        final SOAPMessage jaxwsMessage = message.getContent(javax.xml.soap.SOAPMessage.class);
        try {
            jaxwsMessage.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, message.getContextualProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY));

        } catch (final SOAPException e) {
            PropertyValueExchangeInterceptor.LOG.error("", e);
        }
    }
}


