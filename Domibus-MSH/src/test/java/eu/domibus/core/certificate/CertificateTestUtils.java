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

public class CertificateTestUtils {

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
