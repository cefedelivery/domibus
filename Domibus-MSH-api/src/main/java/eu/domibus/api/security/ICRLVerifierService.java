package eu.domibus.api.security;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Created by feriaad on 18/06/2015.
 */

public interface ICRLVerifierService {
    void verifyCertificateCRLs(X509Certificate cert) throws AuthenticationException;

    List<String> getCrlDistributionPoints(X509Certificate cert) throws AuthenticationException;

    /* TODO - keep this? How do I extract the crlDistributionPointURL */
    void verifyCertificateCRLs(String serial, String crlDistributionPointURL) throws AuthenticationException;
}
