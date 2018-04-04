package eu.domibus.ebms3.receiver;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class SetDomainInInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetDomainInInterceptor.class);

    public SetDomainInInterceptor() {
        this(Phase.RECEIVE);
    }

    @Autowired
    DomainContextProvider domainContextProvider;

    protected SetDomainInInterceptor(String phase) {
        super(phase);
        this.addBefore(SetPolicyInInterceptor.class.getName());
    }

    /**
     * Intercepts a message.
     * Interceptors should NOT invoke handleMessage or handleFault
     * on the next interceptor - the interceptor chain will take care of this.
     *
     * @param message the incoming message to handle
     */
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        HttpServletRequest httpRequest = (HttpServletRequest) message.get("HTTP.REQUEST");
        String domainCode = null;
        try {
            domainCode = ServletRequestUtils.getStringParameter(httpRequest, "domain");
        } catch (ServletRequestBindingException e) {
            EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "Could not determine the domain", null, null);
            ebMS3Ex.setMshRole(MSHRole.RECEIVING);
            throw new Fault(ebMS3Ex);
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(domainCode)) {
            LOG.debug("No domain specified. Using the default domain");
            domainCode = DomainService.DEFAULT_DOMAIN.getCode();
        }

        LOG.debug("Using domain [{}]", domainCode);
        domainContextProvider.setCurrentDomain(domainCode);
    }
}


