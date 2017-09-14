package eu.domibus.ebms3.receiver;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.SoapService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.apache.cxf.binding.soap.HeaderUtil;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This interceptor is responsible for discovery and setup of WS-Security Policies for incoming messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public class SetPolicyInInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyInInterceptor.class);

    private JAXBContext jaxbContext;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private SoapService soapService;

    private MessageLegConfigurationFactory messageLegConfigurationFactory;

    public SetPolicyInInterceptor() {
        this(Phase.RECEIVE);
    }

    protected SetPolicyInInterceptor(String phase) {
        super(phase);
        this.addBefore(PolicyInInterceptor.class.getName());
        this.addAfter(AttachmentInInterceptor.class.getName());
    }

    public void setJaxbContext(final JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public void setMessageLegConfigurationFactory(MessageLegConfigurationFactory messageLegConfigurationFactory) {
        this.messageLegConfigurationFactory = messageLegConfigurationFactory;
    }

    /**
     * Intercepts a message.
     * Interceptors should NOT invoke handleMessage or handleFault
     * on the next interceptor - the interceptor chain will
     * take care of this.
     *
     * @param message the incoming message to handle
     */
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        final String httpMethod = (String) message.get("org.apache.cxf.request.method");
        //TODO add the below logic to a separate interceptor
        if(org.apache.commons.lang.StringUtils.containsIgnoreCase(httpMethod, "GET")) {
            LOG.debug("Detected GET request on MSH: aborting the interceptor chain");
            message.put(SoapMessage.RESPONSE_CODE, 200);
            message.getInterceptorChain().abort();
            return;
        }

        Messaging messaging = null;
        String policyName = null;
        String messageId = null;

        try {

            messaging=soapService.getMessage(message);
            LegConfigurationExtractor legConfigurationExtractor = messageLegConfigurationFactory.extractMessageConfiguration(message, messaging);
            if(legConfigurationExtractor ==null)return;

            final LegConfiguration legConfiguration= legConfigurationExtractor.extractMessageConfiguration();
            final PolicyBuilder builder = message.getExchange().getBus().getExtension(PolicyBuilder.class);
            policyName = legConfiguration.getSecurity().getPolicy();
            final Policy policy = builder.getPolicy(new FileInputStream(new File(domibusConfigurationService.getConfigLocation() + File.separator + "policies", policyName)));
            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_POLICY_INCOMING_USE, policyName);
            //FIXME: the exchange is shared by both the request and the response. This would result in a situation where the policy for an incoming request would be used for the response. I think this is what we want
            message.getExchange().put(PolicyConstants.POLICY_OVERRIDE, policy);
            message.put(PolicyConstants.POLICY_OVERRIDE, policy);
            message.getInterceptorChain().add(new SetPolicyInInterceptor.CheckEBMSHeaderInterceptor());
            message.getInterceptorChain().add(new SetPolicyInInterceptor.SOAPMessageBuilderInterceptor());
            final String securityAlgorithm = legConfiguration.getSecurity().getSignatureMethod().getAlgorithm();
            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
            message.getExchange().put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_ALGORITHM_INCOMING_USE, securityAlgorithm);

        } catch (EbMS3Exception e) {
            setBindingOperation(message);
            SetPolicyInInterceptor.LOG.debug("", e); // Those errors are expected (no PMode found, therefore DEBUG)
            throw new Fault(e);
        } catch (IOException | ParserConfigurationException | SAXException | JAXBException e) {
            setBindingOperation(message);
            LOG.businessError(DomibusMessageCode.BUS_SECURITY_POLICY_INCOMING_NOT_FOUND, e, policyName); // Those errors are not expected
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "no valid security policy found", messaging != null ? messageId : "unknown", e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw new Fault(ex);
        }

    }

    //this is a hack to avoid a nullpointer in @see WebFaultOutInterceptor.
    //I suppose the bindingOperation is set after the execution of this interceptor and is empty in case of error.

    private void setBindingOperation(SoapMessage message) {
        final Exchange exchange = message.getExchange();
        if (exchange == null) {
            return;
        }
        final Endpoint endpoint = exchange.getEndpoint();
        if (endpoint == null) {
            return;
        }
        final EndpointInfo endpointInfo = endpoint.getEndpointInfo();
        if (endpointInfo == null) {
            return;
        }
        final BindingInfo binding = endpointInfo.getBinding();
        if (binding == null) {
            return;
        }
        final Collection<BindingOperationInfo> operations = binding.getOperations();
        if (operations == null) {
            return;
        }
        for (BindingOperationInfo operation : operations) {
            exchange.put(BindingOperationInfo.class, operation);
        }
    }


    public static class CheckEBMSHeaderInterceptor extends AbstractSoapInterceptor {

        public CheckEBMSHeaderInterceptor() {
            super(Phase.PRE_LOGICAL);
            this.addBefore(MustUnderstandInterceptor.MustUnderstandEndingInterceptor.class.getName());

        }


        @Override
        public void handleMessage(final SoapMessage message) {
            HeaderUtil.getHeaderQNameInOperationParam(message).add(ObjectFactory._Messaging_QNAME);
        }

        @Override
        public Set<QName> getUnderstoodHeaders() {
            final Set<QName> understood = new HashSet<>();
            understood.add(ObjectFactory._Messaging_QNAME);
            return understood;
        }
    }

    public static class SOAPMessageBuilderInterceptor extends AbstractSoapInterceptor {

        public SOAPMessageBuilderInterceptor() {
            super(Phase.PRE_LOGICAL);
            this.addAfter(MustUnderstandInterceptor.MustUnderstandEndingInterceptor.class.getName());
        }


        @Override
        public void handleMessage(final SoapMessage message) throws Fault {
            final SOAPMessage result = message.getContent(SOAPMessage.class);
            try {
                SAAJInInterceptor.replaceHeaders(result, message);
                result.removeAllAttachments();
                final Collection<Attachment> atts = message.getAttachments();
                if (atts != null) {
                    for (final Attachment a : atts) {
                        if (a.getDataHandler().getDataSource() instanceof AttachmentDataSource) {
                            try {
                                ((AttachmentDataSource) a.getDataHandler().getDataSource()).cache(message);
                            } catch (final IOException e) {
                                throw new Fault(e);
                            }
                        }
                        final AttachmentPart ap = result.createAttachmentPart(a.getDataHandler());
                        final Iterator<String> i = a.getHeaderNames();
                        while (i != null && i.hasNext()) {
                            final String h = i.next();
                            final String val = a.getHeader(h);
                            ap.addMimeHeader(h, val);
                        }
                        if (StringUtils.isEmpty(ap.getContentId())) {
                            ap.setContentId(a.getId());
                        }
                        result.addAttachmentPart(ap);
                    }
                }

            } catch (final SOAPException soapEx) {
                LOG.error("Could not replace headers for incoming Message", soapEx);
            }

        }
    }

}


