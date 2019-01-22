package eu.domibus.common.util;

import eu.domibus.proxy.DomibusProxy;
import junit.framework.Assert;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.junit.Test;


/**
 * @author idragusa
 * @since 4.0
 */
public class ProxyUtilTest {

    @Tested
    ProxyUtil proxyUtil;

    @Injectable
    protected DomibusProxy domibusProxy;


    @Test
    public void testUseProxyFalse() {
        Assert.assertFalse(domibusProxy.isEnabled());
    }

    @Test
    public void testUseProxytrue() {
        new NonStrictExpectations() {{
            domibusProxy.isEnabled();
            result = true;

            domibusProxy.getHttpProxyHost();
            result = "somehost";

            domibusProxy.getHttpProxyPort();
            result = "8280";

            domibusProxy.getHttpProxyUser();
            result = "someuser";

            domibusProxy.getHttpProxyPassword();
            result = "somepass";

        }};

        Assert.assertTrue(domibusProxy.isEnabled());
    }

    @Test
    public void testGetConfiguredProxy() {
        new NonStrictExpectations() {{
            domibusProxy.isEnabled();
            result = true;

            domibusProxy.getHttpProxyHost();
            result = "somehost";

            domibusProxy.getHttpProxyPort();
            result = "8280";

            domibusProxy.getHttpProxyUser();
            result = "someuser";

            domibusProxy.getHttpProxyPassword();
            result = "somepass";

        }};
        HttpHost httpHost = proxyUtil.getConfiguredProxy();

        Assert.assertEquals(httpHost.getPort(), 8280);
    }

    @Test
    public void testGetConfiguredCredentialsProvider() {
        new NonStrictExpectations() {{
            domibusProxy.isEnabled();
            result = true;

            domibusProxy.getHttpProxyHost();
            result = "somehost";

            domibusProxy.getHttpProxyPort();
            result = "8280";

            domibusProxy.getHttpProxyUser();
            result = "someuser";

            domibusProxy.getHttpProxyPassword();
            result = "somepass";

        }};
        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        Assert.assertEquals("someuser", credentialsProvider.getCredentials(AuthScope.ANY).getUserPrincipal().getName());
    }
}

