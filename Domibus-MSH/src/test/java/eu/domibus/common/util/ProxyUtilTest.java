package eu.domibus.common.util;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import junit.framework.Assert;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.junit.Test;


/**
 * Created by idragusa on 6/5/18.
 */
public class ProxyUtilTest {

    @Tested
    ProxyUtil proxyUtil;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Test
    public void testUseProxyFalse() {
        Assert.assertFalse(domibusConfigurationService.useProxy());
    }

    @Test
    public void testUseProxytrue() {
        new NonStrictExpectations() {{
            domibusConfigurationService.useProxy();
            result = true;

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            result = "somehost";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            result = "8280";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            result = "someuser";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);
            result = "somepass";

        }};

        // exception is expected
        Assert.assertTrue(domibusConfigurationService.useProxy());
    }

    @Test
    public void testGetConfiguredProxy() {
        new NonStrictExpectations() {{
            domibusConfigurationService.useProxy();
            result = true;

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            result = "somehost";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            result = "8280";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            result = "someuser";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);
            result = "somepass";

        }};
        HttpHost httpHost = proxyUtil.getConfiguredProxy();

        Assert.assertEquals(httpHost.getPort(), 8280);
    }

    @Test
    public void testGetConfiguredCredentialsProvider() {
        new NonStrictExpectations() {{
            domibusConfigurationService.useProxy();
            result = true;

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            result = "somehost";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            result = "8280";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            result = "someuser";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);
            result = "somepass";

        }};
        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        Assert.assertEquals("someuser", credentialsProvider.getCredentials(AuthScope.ANY).getUserPrincipal().getName());
    }
}

