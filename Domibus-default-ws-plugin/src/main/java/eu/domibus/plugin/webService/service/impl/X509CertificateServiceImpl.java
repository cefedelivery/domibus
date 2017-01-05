package eu.domibus.plugin.webService.service.impl;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.plugin.webService.common.exception.AuthenticationException;
import eu.domibus.plugin.webService.service.ICRLVerifierService;
import eu.domibus.plugin.webService.service.IX509CertificateService;
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

@Service
public class X509CertificateServiceImpl implements IX509CertificateService {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(X509CertificateServiceImpl.class);

    private static final Locale LOCALE = Locale.US;

    @Autowired
    ICRLVerifierService crlVerifierService;

    @Override
    public boolean isClientX509CertificateValid(final X509Certificate[] certificates) throws AuthenticationException {
        if (certificates != null) {
            Date today = Calendar.getInstance().getTime();
            DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", LOCALE);
            for (final X509Certificate cert : certificates) {
                try {
                    cert.checkValidity();
                } catch (CertificateExpiredException exc) {
                    LOGGER.businessError(DomibusMessageCode.BUS_CERTIFICATE_EXPIRED, df.format(today), df.format(cert.getNotBefore().getTime()), df.format(cert.getNotAfter().getTime()));
                    throw new AuthenticationException("Certificate expired", exc);
                } catch (CertificateNotYetValidException exc) {
                    LOGGER.businessError(DomibusMessageCode.BUS_CERTIFICATE_NOT_YET_VALID, df.format(today), df.format(cert.getNotBefore().getTime()), df.format(cert.getNotAfter().getTime()));
                    throw new AuthenticationException("Certificate not yet valid",  exc);
                }
                crlVerifierService.verifyCertificateCRLs(cert);
            }
        }
        return true;
    }
}
