package eu.domibus.core.security;

import com.google.common.collect.Lists;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.PullRequestMapping;
import eu.domibus.core.crypto.spi.model.UserMessageMapping;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.CertificateExchangeType;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.ebms3.receiver.TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Authorization service that will extract data from the SoapMessage before delegating the authorization
 * call to the configured SPI.
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

    public void authorizePullRequest(SOAPMessage request, PullRequest pullRequest) {
        if (!isAuthorizationEnabled(request)) {
            return;
        }
        final CertificateTrust certificateTrust = getCertificateTrust(request);
        final Map<PullRequestMapping, String> pullRequestMapping;
        try {
            pullRequestMapping = pModeProvider.getPullRequestMapping(pullRequest);
        } catch (EbMS3Exception e) {
            throw new AuthorizationException(e);
        }
        getAuthorizationService().authorize(certificateTrust.getTrustChain(), certificateTrust.getSigningCertificate(),
                domainCoreConverter.convert(pullRequest, PullRequestDTO.class), pullRequestMapping);
    }

    public void authorizeUserMessage(SOAPMessage request, UserMessage userMessage) {
        if (!isAuthorizationEnabled(request)) {
            return;
        }
        final CertificateTrust certificateTrust = getCertificateTrust(request);
        final Map<UserMessageMapping, String> userMessageMapping;
        try {
            userMessageMapping = pModeProvider.getUserMessageMapping(userMessage);
        } catch (EbMS3Exception e) {
            throw new AuthorizationException(e);
        }
        getAuthorizationService().authorize(certificateTrust.getTrustChain(), certificateTrust.getSigningCertificate(),
                domainCoreConverter.convert(userMessage, UserMessageDTO.class), userMessageMapping);

    }

    private boolean isAuthorizationEnabled(SOAPMessage request) {
        if (!domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING)) {
            LOG.debug("No trust verification of sending certificate");
            return false;
        }
        final CertificateExchangeType certificateExchangeType = getCertificateExchangeTypeFromSoapMessage(request);
        if (CertificateExchangeType.NONE.equals(certificateExchangeType)) {
            LOG.debug("Message has no security configured, skipping authorization");
            return false;
        }
        return true;
    }

    private CertificateTrust getCertificateTrust(SOAPMessage request) {
        final List<X509Certificate> x509Certificates = getCertificatesFromSoapMessage(request);
        X509Certificate leafCertificate = (X509Certificate) certificateService.extractLeafCertificateFromChain(x509Certificates);
        final List<X509Certificate> signingCertificateTrustChain = Lists.newArrayList(x509Certificates);
        signingCertificateTrustChain.remove(leafCertificate);
        final CertificateTrust certificateTrust = new CertificateTrust(leafCertificate, signingCertificateTrustChain);
        return certificateTrust;
    }

    private List<X509Certificate> getCertificatesFromSoapMessage(SOAPMessage request) {
        String certificateChainValue;
        try {
            certificateChainValue = (String) request.getProperty(CertificateExchangeType.getValue());
        } catch (SOAPException e) {
            throw new IllegalStateException(String.
                    format("At this stage, the property:[%s] of the soap message should contain a certificate", CertificateExchangeType.getValue()), e);
        }
        return certificateService.deserializeCertificateChainFromPemFormat(certificateChainValue);

    }

    private CertificateExchangeType getCertificateExchangeTypeFromSoapMessage(SOAPMessage request) {
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
