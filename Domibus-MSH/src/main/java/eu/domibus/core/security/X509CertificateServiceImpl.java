package eu.domibus.core.security;

import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.X509CertificateService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.pki.CRLService;
import eu.domibus.pki.DomibusCRLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Service("securityX509CertificateServiceImpl")
public class X509CertificateServiceImpl implements X509CertificateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(X509CertificateServiceImpl.class);

    private static final Locale LOCALE = Locale.US;

    @Autowired
    CRLService crlService;

    @Override
    public boolean isClientX509CertificateValid(final X509Certificate[] certificates) throws AuthenticationException {
        if (certificates != null) {
            Date today = Calendar.getInstance().getTime();
            DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", LOCALE);
            for (final X509Certificate cert : certificates) {
                try {
                    cert.checkValidity();
                } catch (CertificateExpiredException exc) {
                    LOG.securityInfo(DomibusMessageCode.SEC_CERTIFICATE_EXPIRED, df.format(today), df.format(cert.getNotBefore().getTime()), df.format(cert.getNotAfter().getTime()));
                    throw new AuthenticationException("Certificate expired", exc);
                } catch (CertificateNotYetValidException exc) {
                    LOG.securityInfo(DomibusMessageCode.SEC_CERTIFICATE_NOT_YET_VALID, df.format(today), df.format(cert.getNotBefore().getTime()), df.format(cert.getNotAfter().getTime()));
                    throw new AuthenticationException("Certificate not yet valid", exc);
                }
                try {
                    if (crlService.isCertificateRevoked(cert)) {
                        throw new AuthenticationException("The certificate is revoked.");
                    }
                } catch (DomibusCRLException ex) {
                    throw new AuthenticationException("Cannot verify CRL for certificate:", ex);
                }
            }
        }
        return true;
    }
}