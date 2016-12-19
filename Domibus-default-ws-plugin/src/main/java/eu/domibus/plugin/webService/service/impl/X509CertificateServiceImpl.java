package eu.domibus.plugin.webService.service.impl;

import eu.domibus.security.AuthenticationException;
import eu.domibus.plugin.webService.service.ICRLVerifierService;
import eu.domibus.plugin.webService.service.IX509CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
public class X509CertificateServiceImpl implements IX509CertificateService {

    @Autowired
    ICRLVerifierService crlVerifierService;

    @Override
    public boolean isClientX509CertificateValid(final X509Certificate[] certificates) throws AuthenticationException {
        if(certificates != null)
            for (final X509Certificate cert : certificates) {
                try {
                    cert.checkValidity();
                } catch (CertificateException e){
                    throw new AuthenticationException("Certificate validity test failed: " + e);

                }

                crlVerifierService.verifyCertificateCRLs(cert);
            }
        return true;
    }
}
