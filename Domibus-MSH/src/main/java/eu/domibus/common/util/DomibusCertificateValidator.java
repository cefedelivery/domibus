package eu.domibus.common.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.security.CertificateException;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import java.security.cert.X509Certificate;

/**
 * @author idragusa
 * @since 4.1
 * Provide our own certificate validator to be used by difi client for SMP certificate validation.
 * Default difi certificate validator does not have a way to configure proxy for CRL verification
 */
public class DomibusCertificateValidator implements CertificateValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCertificateValidator.class);
    protected CertificateService certificateService;

    public DomibusCertificateValidator(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    // validate the SMP certificate
    public void validate(Service service, X509Certificate certificate) throws PeppolSecurityException {
        LOG.debug("Certificate validator for certificate: [{}]", getSubjectDN(certificate));
        if (!certificateService.isCertificateValid(certificate)) {
            throw new PeppolSecurityException("Lookup certificate validator failed for " + getSubjectDN(certificate));
        }
    }

    protected String getSubjectDN(X509Certificate cert) {
        if (cert != null && cert.getSubjectDN() != null) {
            return cert.getSubjectDN().getName();
        }
        return null;
    }
}
