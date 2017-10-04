package eu.domibus.core.security;

import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.CertificateDetails;
import eu.domibus.api.security.BlueCoatCertificateService;
import org.springframework.stereotype.Component;

@Component("securityBlueCoatCertificateServiceImpl")
public class BlueCoatCertificateServiceImpl implements BlueCoatCertificateService {

    /**
     * Validate the certificate.
     *
     * @param certificate The certificate to validate.
     * @return <code>true</code> if valid, <code>false</code> otherwise.
     */
    public boolean isBlueCoatClientCertificateValid(final CertificateDetails certificate) throws AuthenticationException {
        /* for now we consider the certificate was validated at TLS level */
        return true;
    }
}
