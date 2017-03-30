package eu.domibus.api.security;


import java.security.cert.X509Certificate;

public interface X509CertificateService {

    boolean isClientX509CertificateValid(final X509Certificate[] certificate) throws AuthenticationException;
}
