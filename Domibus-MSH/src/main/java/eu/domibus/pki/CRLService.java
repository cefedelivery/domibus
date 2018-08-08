package eu.domibus.pki;

import org.springframework.cache.annotation.Cacheable;

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

    /**
     * Checks the pki revocation status against the provided distribution point.
     * Supports HTTP, HTTPS, FTP, File based URLs.
     * @param serialString the pki serial number
     * @param crlDistributionPointURL the certificate revocation list url
     * @return true if the pki is revoked
     * @throws DomibusCRLException if an error occurs while downloading the certificate revocation list
     */
    boolean isCertificateRevoked(String serialString, String crlDistributionPointURL) throws DomibusCRLException;
}
