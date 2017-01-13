package eu.domibus.ebms3.sender;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.common.ErrorCode;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.PolicyFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.attachment.AttachmentUtil;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.apache.cxf.ws.policy.PolicyVerificationOutInterceptor;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.wsdl.interceptors.BareOutInterceptor;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This interceptor is responsible for discovery and setup of WS-Security Policies for outgoing messages
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public class SetPolicyOutInterceptor extends AbstractSoapInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyOutInterceptor.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PolicyFactory policyFactory;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;


    public SetPolicyOutInterceptor() {
        super(Phase.SETUP);
        this.addBefore(PolicyInInterceptor.class.getName());
    }

    /**
     * Intercepts a message.
     * Interceptors should NOT invoke handleMessage or handleFault
     * on the next interceptor - the interceptor chain will
     * take care of this.
     *
     * @param message the message to handle
     */
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        LOG.debug("SetPolicyOutInterceptor");

        final String pModeKey = (String) message.getContextualProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
        message.getInterceptorChain().add(new SetPolicyOutInterceptor.PrepareAttachmentInterceptor());

        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pModeKey);

        message.put(SecurityConstants.USE_ATTACHMENT_ENCRYPTION_CONTENT_ONLY_TRANSFORM, true);

        final String securityAlgorithm = legConfiguration.getSecurity().getSignatureMethod().getAlgorithm();
        message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
        LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_ALGORITHM_OUTGOING_USE, securityAlgorithm);
        final String encryptionUsername = pModeProvider.getReceiverParty(pModeKey).getName();
        message.put(SecurityConstants.ENCRYPT_USERNAME, encryptionUsername);
        LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_USER_OUTGOING_USE, encryptionUsername);

        try {
            final Policy policy = policyFactory.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_POLICY_OUTGOING_USE, legConfiguration.getSecurity().getPolicy());
            message.put(PolicyConstants.POLICY_OVERRIDE, policy);
        } catch (final ConfigurationException e) {
            LOG.businessError(DomibusMessageCode.BUS_SECURITY_POLICY_OUTGOING_NOT_FOUND, e, legConfiguration.getSecurity().getPolicy());
            throw new Fault(new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "Could not find policy file " + domibusConfigurationService.getConfigLocation() + "/" + this.pModeProvider.getLegConfiguration(pModeKey).getSecurity(), null, null));
        }

    }

    static class PrepareAttachmentInterceptor extends AbstractPhaseInterceptor<Message> {
        public PrepareAttachmentInterceptor() {
            super(Phase.MARSHAL);
            super.addBefore(BareOutInterceptor.class.getName());
        }

        /**
         * Intercepts a message.
         * Interceptors should NOT invoke handleMessage or handleFault
         * on the next interceptor - the interceptor chain will
         * take care of this.
         *
         * @param message message to handle
         */
        @Override
        public void handleMessage(final Message message) throws Fault {
            /*InterceptorChain interceptorChain = message.getInterceptorChain();
            Interceptor wss4jInternal = null;
            PolicyBasedWSS4JOutInterceptor wss4JOutInterceptor = null;

            for(Interceptor interceptor : interceptorChain){
                LOG.info(interceptor.getClass().getName());
            }

            for(Interceptor interceptor : interceptorChain){
                if(interceptor.getClass().getName().contains("WSS4JOutInterceptorInternal")){
                    wss4jInternal = interceptor;
                }
                if (interceptor instanceof PolicyBasedWSS4JOutInterceptor){
                    wss4JOutInterceptor = (PolicyBasedWSS4JOutInterceptor)interceptor;
                }

            }
            interceptorChain.remove(wss4jInternal);
            interceptorChain.add(wss4JOutInterceptor.createEndingInterceptor());

           for(Interceptor interceptor : interceptorChain){
                LOG.info(interceptor.getClass().getName());
            }*/

            final SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
            if (soapMessage.countAttachments() > 0) {
                if (message.getAttachments() == null) {
                    message.setAttachments(new ArrayList<Attachment>(soapMessage
                            .countAttachments()));
                }
                final Iterator<AttachmentPart> it = CastUtils.cast(soapMessage.getAttachments());
                while (it.hasNext()) {
                    final AttachmentPart part = it.next();
                    final String id = AttachmentUtil.cleanContentId(part.getContentId());
                    final AttachmentImpl att = new AttachmentImpl(id);
                    try {
                        att.setDataHandler(part.getDataHandler());
                    } catch (final SOAPException e) {
                        throw new Fault(e);
                    }
                    final Iterator<MimeHeader> it2 = CastUtils.cast(part.getAllMimeHeaders());
                    while (it2.hasNext()) {
                        final MimeHeader header = it2.next();
                        att.setHeader(header.getName(), header.getValue());
                    }
                    message.getAttachments().add(att);
                }
            }
            message.getInterceptorChain().add(new SetPolicyOutInterceptor.LogAfterPolicyCheckInterceptor());


        }

    }

    public static class LogAfterPolicyCheckInterceptor extends AbstractSoapInterceptor {


        public LogAfterPolicyCheckInterceptor() {
            super(Phase.POST_STREAM);
            this.addAfter(PolicyVerificationOutInterceptor.class.getName());
        }

        @Override
        public void handleMessage(final SoapMessage message) throws Fault {

            final SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
            soapMessage.removeAllAttachments();
        }
    }
}
