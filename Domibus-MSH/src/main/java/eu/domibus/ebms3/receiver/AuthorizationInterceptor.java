package eu.domibus.ebms3.receiver;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.SoapService;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.crypto.spi.model.UserMessage;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuthorizationInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Autowired
    private List<AuthorizationServiceSpi> authorizationServiceSpis;

    @Autowired
    private SoapService soapService;

    @Autowired
    private BindingHelper bindingHelper;

    @Autowired
    private DomibusPropertiesService domibusPropertiesService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    protected static final String IAM_AUTHORIZATION_IDENTIFIER = "domibus.extension.iam.authorization.identifier";

    public AuthorizationInterceptor() {
        this(Phase.RECEIVE);
    }

    protected AuthorizationInterceptor(String phase) {
        super(phase);
        this.addBefore(SetPolicyInInterceptor.class.getName());
        this.addAfter(ServiceInterceptor.class.getName());
    }

    AuthorizationServiceSpi getAuthorizationService() {
        final String authorizationServiceIndentifier = domibusPropertyProvider.getDomainProperty(IAM_AUTHORIZATION_IDENTIFIER);
        final List<AuthorizationServiceSpi> authorizationServiceList = this.authorizationServiceSpis.stream().
                filter(authorizationServiceSpi -> authorizationServiceIndentifier.equals(authorizationServiceSpi.getIdentifier())).
                collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authorization spi:");
            authorizationServiceList.forEach(authorizationServiceSpi -> LOG.debug(" identifier:[{}] for class:[{}]", authorizationServiceSpi.getIdentifier(), authorizationServiceSpi.getClass()));
        }

        if (authorizationServiceList.size() > 1) {
            throw new IllegalStateException(String.format("More than one authorization service provider for identifier:[%s]", authorizationServiceIndentifier));
        }
        if (authorizationServiceList.isEmpty()) {
            throw new IllegalStateException(String.format("No authorisation service provider found for given identifier:[%s]", authorizationServiceIndentifier));
        }
        return authorizationServiceList.get(0);
    }

    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {
        Messaging message = null;
        try {
            message = soapService.getMessage(soapMessage);
            getAuthorizationService().authorize(null, domainCoreConverter.convert(message.getUserMessage(), UserMessage.class));
        } catch (EbMS3Exception e) {
            bindingHelper.setBindingOperation(soapMessage);
            LOG.debug("", e); // Those errors are expected (no PMode found, therefore DEBUG)
            throw new Fault(e);
        } catch (IOException | JAXBException e) {
            bindingHelper.setBindingOperation(soapMessage);
            //LOG.businessError(DomibusMessageCode.BUS_SECURITY_POLICY_INCOMING_NOT_FOUND, e, policyName); // Those errors are not expected
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "no valid security policy found", message != null ? message.getUserMessage().getMessageInfo().getMessageId() : "unknown", e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw new Fault(ex);
        }

    }
}
