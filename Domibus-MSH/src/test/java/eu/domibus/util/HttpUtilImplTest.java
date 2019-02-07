package eu.domibus.util;

import eu.domibus.common.util.ProxyUtil;
import eu.domibus.proxy.DomibusProxyService;
import eu.domibus.proxy.DomibusProxyServiceImpl;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

/**
 * @author idragusa
 * @since 4.1
 */
@RunWith(JMockit.class)
@Ignore
public class HttpUtilImplTest {
    @Tested
    HttpUtilImpl httpUtil;

    @Injectable
    ProxyUtil proxyUtil;

    @Injectable
    DomibusProxyService domibusProxyService;

    //@Test
    public void testDownloadCRLViaProxy() throws Exception {
        new NonStrictExpectations(proxyUtil) {{
            domibusProxyService.useProxy();
            result = true;

            proxyUtil.getConfiguredProxy();
            result = new HttpHost("158.169.9.13", 8012);

            proxyUtil.getConfiguredCredentialsProvider();
            result = getTestCredentialsProvider();

        }};
        String url = "http://onsitecrl.verisign.com/offlineca/NATIONALITANDTELECOMAGENCYPEPPOLRootCA.crl";
        ByteArrayInputStream inputStream = httpUtil.downloadURLDirect(url);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL x509CRL = (X509CRL) cf.generateCRL(inputStream);
        System.out.println(x509CRL);

        inputStream = httpUtil.downloadURLViaProxy(url);
        System.out.println(inputStream);
        x509CRL = (X509CRL) cf.generateCRL(inputStream);
        System.out.println(x509CRL);

    }

    protected CredentialsProvider getTestCredentialsProvider() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope("158.169.9.13", 8012),
                new UsernamePasswordCredentials("baciuco", "pass"));

        return credsProvider;
    }
}
