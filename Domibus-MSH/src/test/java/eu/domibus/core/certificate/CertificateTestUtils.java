package eu.domibus.core.certificate;

import eu.domibus.pki.DomibusCertificateException;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Utility methods class used in tests that need to work with certificates
 *
 * @author Ion Perpegel
 * @since 4.1
 */
public class CertificateTestUtils {

    /**
     * Loads a certificate from a JKS file
     * @param filePath path to the file representing the keystore
     * @param password the password to open the keystore
     * @param alias the name of the certificate
     */
    public static X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, password.toCharArray());

            Certificate cert = keyStore.getCertificate(alias);

            return (X509Certificate) cert;
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new DomibusCertificateException("Could not load certificate from file " + filePath + ", alias " + alias, e);
        }
    }
}
