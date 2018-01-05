package eu.domibus.taxud.authentication;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuthenticationClient {

    public static void main(String[] args) {


    }

    public void authenticate(){
        try {
            SSLContext sslContext = SSLContexts.custom()
                    // keystore wasn't within the question's scope, yet it might be handy:
                    .loadKeyMaterial(
                            keyStore.getFile(),
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
