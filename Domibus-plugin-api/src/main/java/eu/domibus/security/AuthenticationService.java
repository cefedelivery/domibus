package eu.domibus.security;

/**
 * @author baciu
 */
public interface AuthenticationService {

    void authenticate(BasicAuthentication authentication);

    void authenticate(X509CertificateAuthentication authentication);

    void authenticate(BlueCoatClientCertificateAuthentication authentication);
}
