package eu.domibus.ebms3.receiver;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.certificate.Certificate;
import eu.domibus.common.services.SoapService;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.ebms3.receiver.TrustSenderInterceptor.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING;
import static eu.domibus.ebms3.receiver.TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING;

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
    private CertificateService certificateService;

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

    public void authorizeUserMessage(SOAPMessage request, Messaging messaging) throws EbMS3Exception {
        authorize(request, messaging, messaging.getUserMessage().getMessageInfo().getMessageId());
    }

    private void authorize(SOAPMessage request, Messaging messaging, String messageId) throws EbMS3Exception {
        if (!domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING)) {
            LOG.debug("No trust verification of sending certificate");
            return;
        }

        String certificateExchangeTypeValue;
        String certificateChainValue;
        try {
            certificateExchangeTypeValue = (String) request.getProperty(CertificateExchangeType.getKey());
            certificateChainValue = (String) request.getProperty(CertificateExchangeType.getValue());
        } catch (SOAPException e) {
            throw new IllegalStateException(String.
                    format("At this stage, the property:[%s] of the soap message should contain a certificate", CertificateExchangeType.getValue()), e);
        }

        try {
            final CertificateExchangeType certificateExchangeType1 = CertificateExchangeType.valueOf(certificateExchangeTypeValue);
            if (CertificateExchangeType.NONE.equals(certificateExchangeType1)) {
                LOG.info("Message does not contain security info ==> skipping sender trust verification.");
                return;
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("Invalid certificate exchange type:[%s]", certificateExchangeTypeValue));
        }

        final List<X509Certificate> x509Certificates = certificateService.deserializeCertificateChain(certificateChainValue);
        if (domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING)) {
            checkCertificateValidity(x509Certificates, messageId);
        }
        getAuthorizationService().authorize(x509Certificates.toArray(new X509Certificate[]{}), domainCoreConverter.convert(messaging.getUserMessage(), eu.domibus.core.crypto.spi.model.UserMessage.class));
    }

    protected void checkCertificateValidity(List<X509Certificate> certificates, String messageId) throws EbMS3Exception {
        for (X509Certificate certificate : certificates) {
            try {
                if (!certificateService.isCertificateValid(certificate)) {
                    LOG.error("Invalid incoming certificate");
                    EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, String.format("Certificate with subject [%s] certificate is not valid or has been revoked", certificate.getSubjectDN()), messageId, null);
                    ebMS3Ex.setMshRole(MSHRole.RECEIVING);
                    throw ebMS3Ex;
                }
            } catch (DomibusCertificateException dce) {
                LOG.error("Invalid incoming certificate");
                EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, String.format("Invalid incoming certificate:[%s]", certificate.getSubjectDN()), messageId, null);
                ebMS3Ex.setMshRole(MSHRole.RECEIVING);
                throw ebMS3Ex;
            }
        }
    }

}
