package eu.domibus.pki;

import java.security.cert.X509Certificate;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
public interface CRLService {

    /**
     * Extracts the CRL distribution points from the pki (if available)
     * and checks the pki revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP, File based URLs.
     *
     * @param cert the pki to be checked for revocation
     * @throws DomibusCRLException if the CRLs from the pki could not be retrieved
     * @return true if the pki is revoked
     */
    boolean isCertificateRevoked(X509Certificate cert) throws DomibusCRLException;

}
