package eu.domibus.ebms3.receiver;

import com.google.common.collect.Lists;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.ebms3.receiver.TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@Component
public class AuthorizationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthorizationService.class);

    protected static final String IAM_AUTHORIZATION_IDENTIFIER = "domibus.extension.iam.authorization.identifier";

    @Autowired
    private List<AuthorizationServiceSpi> authorizationServiceSpis;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private PModeProvider pModeProvider;

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

    public void authorizeUserMessage(SOAPMessage request, UserMessage userMessage) throws EbMS3Exception {

        if (!domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING)) {
            LOG.debug("No trust verification of sending certificate");
            return;
        }
        final CertificateExchangeType certificateExchangeType = getCertificateExchangeType(request);
        if (CertificateExchangeType.NONE.equals(certificateExchangeType)) {
            LOG.debug("Message has no security configured, skipping authorization");
            return;
        }

        final List<X509Certificate> x509Certificates = getCertificates(request);
        X509Certificate leafCertificate = (X509Certificate) certificateService.extractLeafCertificateFromChain(x509Certificates);
        final List<X509Certificate> signingCertificateTrustChain= Lists.newArrayList(x509Certificates);
        signingCertificateTrustChain.remove(leafCertificate);
        getAuthorizationService().authorize(signingCertificateTrustChain,leafCertificate,
                domainCoreConverter.convert(userMessage, UserMessageDTO.class), pModeProvider.getMessageMapping(userMessage));
    }

    private List<X509Certificate> getCertificates(SOAPMessage request) {
        String certificateChainValue;
        try {
            certificateChainValue = (String) request.getProperty(CertificateExchangeType.getValue());
        } catch (SOAPException e) {
            throw new IllegalStateException(String.
                    format("At this stage, the property:[%s] of the soap message should contain a certificate", CertificateExchangeType.getValue()), e);
        }
        return certificateService.deserializeCertificateChain(certificateChainValue);

    }

    private CertificateExchangeType getCertificateExchangeType(SOAPMessage request) {
        String certificateExchangeTypeValue;
        try {
            certificateExchangeTypeValue = (String) request.getProperty(CertificateExchangeType.getKey());
        } catch (SOAPException e) {
            throw new IllegalStateException(String.
                    format("At this stage, the property:[%s] of the soap message should contain a certificate", CertificateExchangeType.getValue()), e);
        }

        try {
            return CertificateExchangeType.valueOf(certificateExchangeTypeValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("Invalid certificate exchange type:[%s]", certificateExchangeTypeValue));
        }
    }


}
