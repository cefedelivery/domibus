package eu.domibus.taxud.authentication;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Base64;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuthenticationClient {

    public static void main(String[] args) {


    }

    public void authenticate() {
        KeyStore trustStore = null;
        FileInputStream keyStoreStream;
        try {
            keyStoreStream = new FileInputStream("C:\\install\\domains\\12.1.3\\red\\conf\\domibus\\keystores\\gateway_keystore.jks");
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(keyStoreStream, "test123".toCharArray());
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        finally {
            if(keyStoreStream!=null) {
                keyStoreStream.close();
            }
        }

        try {
            Certificate blue_gw = trustStore.getCertificate("blue_gw");
            Base64.getEncoder().encode(blue_gw.getEncoded());

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }

        try {
            SSLContext sslContext = SSLContexts.custom()
                    // keystore wasn't within the question's scope, yet it might be handy:
                    .loadKeyMaterial(
                            trustStore.getFile(),
                            keyStorePassword.toCharArray(),
                            keyStorePassword.toCharArray())
                    .loadTrustMaterial(
                            trustStore.getURL(),
                            keyStorePassword.toCharArray(),
                            // use this for self-signed certificates only:
                            new TrustSelfSignedStrategy())
                    .build();

            HttpClient httpClient = HttpClients.custom()
                    // use NoopHostnameVerifier with caution, see https://stackoverflow.com/a/22901289/3890673
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
