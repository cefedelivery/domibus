package org.apache.cxf.jaxws.handler.soap;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.HeaderUtil;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.handler.AbstractProtocolHandlerInterceptor;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;


/*[EDELIVERY-1117] This interceptor replaces the CXF interceptor SOAPHandlerFaultOutInterceptor.
* The purpose is to have a similar interceptor with the only difference that
* on RuntimeException, the DomibusSOAPHandlerFaultOutInterceptor sets the Code value to "Receiver"
* instead of "HandleFault" which is non standard in SOAP1_2 and raises an error.
* According to the SOAP specification, Code element must have a value.
*
* */
public class DomibusSOAPHandlerFaultOutInterceptor extends
        AbstractProtocolHandlerInterceptor<SoapMessage> implements
        SoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusSOAPHandlerFaultOutInterceptor.class);

    private static final SAAJOutInterceptor SAAJ_OUT = new SAAJOutInterceptor();
    private static final String ENDING_ID = DomibusSOAPHandlerFaultOutInterceptor.class.getName() + ".ENDING";

    AbstractSoapInterceptor ending = new AbstractSoapInterceptor(ENDING_ID, Phase.USER_PROTOCOL) {
        public void handleMessage(SoapMessage message) throws Fault {
            handleMessageInternal(message);
        }
    };

    public DomibusSOAPHandlerFaultOutInterceptor(Binding binding) {
        super(binding, Phase.PRE_PROTOCOL_FRONTEND);
    }

    public Set<URI> getRoles() {
        Set<URI> roles = new HashSet<URI>();
        // TODO
        return roles;
    }

    public Set<QName> getUnderstoodHeaders() {
        Set<QName> understood = new HashSet<QName>();
        for (Handler<?> h : getBinding().getHandlerChain()) {
            if (h instanceof SOAPHandler) {
                Set<QName> headers = CastUtils.cast(((SOAPHandler<?>) h).getHeaders());
                if (headers != null) {
                    understood.addAll(headers);
                }
            }
        }
        return understood;
    }

    public void handleMessage(SoapMessage message) {

        if (binding.getHandlerChain().isEmpty()) {
            return;
        }
        if (getInvoker(message).getProtocolHandlers().isEmpty()) {
            return;
        }

        checkUnderstoodHeaders(message);

        if (getInvoker(message).isOutbound()) {
            //The SOAPMessage might be set from the outchain, in this case,
            //we need to clean it up and create a new SOAPMessage dedicated to fault.
            message.setContent(SOAPMessage.class, null);

            SAAJ_OUT.handleMessage(message);

            message.getInterceptorChain().add(ending);
        }
    }

    private void checkUnderstoodHeaders(SoapMessage soapMessage) {
        Set<QName> paramHeaders = HeaderUtil.getHeaderQNameInOperationParam(soapMessage);
        if (soapMessage.getHeaders().isEmpty() && paramHeaders.isEmpty()) {
            //the TCK expects the getHeaders method to always be
            //called.   If there aren't any headers in the message,
            //THe MustUnderstandInterceptor quickly returns without
            //trying to calculate the understood headers.   Thus,
            //we need to call it here.
            getUnderstoodHeaders();
        }
    }

    private void handleMessageInternal(SoapMessage message) {
        MessageContext context = createProtocolMessageContext(message);
        HandlerChainInvoker invoker = getInvoker(message);
        invoker.setProtocolMessageContext(context);

        try {
            if (!invoker.invokeProtocolHandlersHandleFault(isRequestor(message), context)) {
                // handleAbort(message, context);
            }
        } catch (RuntimeException exception) {
            /*
             * handleFault throws exception, in this case we need to replace
             * SOAPFault with the exception thrown from HandleFault so that the
             * exception can be dispatched.
             */
            try {
                SOAPMessage originalMsg = message.getContent(SOAPMessage.class);
                SOAPBody body = originalMsg.getSOAPPart().getEnvelope().getBody();
                body.removeContents();

                SOAPFault soapFault = body.addFault();

                if (exception instanceof SOAPFaultException) {
                    SOAPFaultException sf = (SOAPFaultException) exception;
                    soapFault.setFaultString(sf.getFault().getFaultString());
                    SAAJUtils.setFaultCode(soapFault, sf.getFault().getFaultCodeAsQName());
                    soapFault.setFaultActor(sf.getFault().getFaultActor());
                    if (sf.getFault().hasDetail()) {
                        Node nd = originalMsg.getSOAPPart().importNode(
                                sf.getFault().getDetail()
                                        .getFirstChild(), true);
                        soapFault.addDetail().appendChild(nd);
                    }
                } else if (exception instanceof Fault) {
                    SoapFault sf = SoapFault.createFault((Fault) exception, message
                            .getVersion());
                    soapFault.setFaultString(sf.getReason());
                    SAAJUtils.setFaultCode(soapFault, sf.getFaultCode());
                    if (sf.hasDetails()) {
                        soapFault.addDetail();
                        Node nd = originalMsg.getSOAPPart().importNode(sf.getDetail(), true);
                        nd = nd.getFirstChild();
                        while (nd != null) {
                            soapFault.getDetail().appendChild(nd);
                            nd = nd.getNextSibling();
                        }
                    }
                } else {
                    soapFault.setFaultString(exception.getMessage());
                    /* This is the place where the fault  */
                    SAAJUtils.setFaultCode(soapFault,
                            SOAPConstants.SOAP_RECEIVER_FAULT);
                }
            } catch (SOAPException e) {
                // do nothing
                LOG.error("Could not set the FaultCode value!", e);
            }
        }

        onCompletion(message);
    }

    @Override
    protected MessageContext createProtocolMessageContext(SoapMessage message) {
        return new SOAPMessageContextImpl(message);
    }

    public void handleFault(SoapMessage message) {
    }

}

