package eu.domibus.pki;

import eu.domibus.wss4j.common.crypto.CryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * @Author Cosmin Baciu
 * @Since 3.2
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    CRLService crlService;

    @Autowired
    CryptoService cryptoService;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Cacheable(value = "certValidationByAlias", key = "#alias")
    @Override
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateException {
        LOG.debug("Checking certificate validation for [" + alias + "]");
        KeyStore trustStore = cryptoService.getTrustStore();
        if (trustStore == null) {
            throw new DomibusCertificateException("Error getting the truststore");
        }

        X509Certificate[] certificateChain = null;
        try {
            certificateChain = getCertificateChain(trustStore, alias);
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Error getting the certificate chain from the truststore for [" + alias + "]", e);
        }
        if (certificateChain == null || certificateChain.length == 0 || certificateChain[0] == null) {
            throw new DomibusCertificateException("Could not find alias in the truststore [" + alias + "]");
        }

        for (X509Certificate certificate : certificateChain) {
            boolean certificateValid = isCertificateValid(certificate);
            if (!certificateValid) {
                return false;
            }
        }

        return true;
    }

    protected X509Certificate[] getCertificateChain(KeyStore trustStore, String alias) throws KeyStoreException {
        //TODO get the certificate chain manually based on the issued by info from the original certificate
        X509Certificate[] certificateChain = (X509Certificate[]) trustStore.getCertificateChain(alias);
        if (certificateChain == null) {
            X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
            certificateChain = new X509Certificate[]{certificate};
        }
        return certificateChain;
    }

    @Override
    public boolean isCertificateValidationEnabled() {
        String certificateValidationEnabled = domibusProperties.getProperty("domibus.certificate.validation.enabled", "true");
        return Boolean.valueOf(certificateValidationEnabled);
    }

    @Override
    public boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException {
        boolean isValid = checkValidity(cert);
        if (!isValid) {
            LOG.warn("Certificate is not valid: " + cert);
            return false;
        }
        try {
            return !crlService.isCertificateRevoked(cert);
        } catch (Exception e) {
            throw new DomibusCertificateException(e);
        }
    }

    protected boolean checkValidity(X509Certificate cert) {
        boolean result = false;
        try {
            cert.checkValidity();
            result = true;
        } catch (Exception e) {
            LOG.warn("Certificate is not valid " + cert, e);
        }

        return result;
    }

    /**
     * Verifies the existence and validity of a certificate.
     *
     * @Author Federico Martini
     * @Since 3.3
     * @param alias
     * @return boolean
     * @throws DomibusCertificateException
     */
    @Override
    public boolean isCertificateValid(String alias) throws DomibusCertificateException {
        LOG.debug("Verifying the certificate with alias [" + alias + "]");
        try {
            X509Certificate certificate = (X509Certificate) cryptoService.getCertificateFromKeystore(alias);
            if (certificate == null) {
                throw new DomibusCertificateException("Error: the certificate does not exist for alias[" + alias + "]");
            }
            if (!isCertificateValid(certificate)) {
                throw new DomibusCertificateException("Error: the certificate is not valid anymore for alias [" + alias + "]");
            }
        } catch (KeyStoreException ksEx) {
            throw new DomibusCertificateException("Error getting the certificate from keystore for alias [" + alias + "]", ksEx);
        }
        return true;
    }

    @Override
    public String extractCommonName(final X509Certificate certificate) throws InvalidNameException {

        final String dn = certificate.getSubjectDN().getName();
        LOG.debug("DN is: " + dn);
        final LdapName ln = new LdapName(dn);
        for (final Rdn rdn : ln.getRdns()) {
            if (StringUtils.equalsIgnoreCase(rdn.getType(), "CN")) {
                LOG.debug("CN is: " + rdn.getValue());
                return rdn.getValue().toString();
            }
        }
        throw new IllegalArgumentException("The certificate does not contain a common name (CN): " + certificate.getSubjectDN().getName());
    }


    /**
     * Load certificate with alias from JKS file and return as {@code X509Certificate}.
     *
     * @param filePath
     * @param alias
     * @param password
     * @return
     */
    @Override
    public X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, password.toCharArray());

            Certificate cert = keyStore.getCertificate(alias);

            return (X509Certificate) cert;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            LOG.error("Could not load certificate from file " + filePath + ", alias " + alias + "pass " + password);
            throw new DomibusCertificateException("Could not load certificate from file " + filePath + ", alias " + alias + "pass " + password, e);
        }
    }
}
