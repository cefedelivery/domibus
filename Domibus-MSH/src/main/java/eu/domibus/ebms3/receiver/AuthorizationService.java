package eu.domibus.ebms3.receiver;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.services.SoapService;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class AuthorizationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthorizationService.class);

    @Autowired
    private List<AuthorizationServiceSpi> authorizationServiceSpis;

    @Autowired
    private SoapService soapService;

    @Autowired
    private DomibusPropertiesService domibusPropertiesService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    protected static final String IAM_AUTHORIZATION_IDENTIFIER = "domibus.extension.iam.authorization.identifier";

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

    public boolean authorize(X509Certificate[] certs, UserMessage userMessage) {
        return getAuthorizationService().authorize(certs, domainCoreConverter.convert(userMessage, eu.domibus.core.crypto.spi.model.UserMessage.class));
    }
}
