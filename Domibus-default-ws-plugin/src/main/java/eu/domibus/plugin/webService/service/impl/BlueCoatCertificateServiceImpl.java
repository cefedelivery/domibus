package eu.domibus.plugin.webService.service.impl;

import eu.domibus.plugin.webService.common.exception.AuthenticationException;
import eu.domibus.plugin.webService.security.CertificateDetails;
import eu.domibus.plugin.webService.service.IBlueCoatCertificateService;
import org.springframework.stereotype.Component;

@Component
public class BlueCoatCertificateServiceImpl implements IBlueCoatCertificateService {

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
