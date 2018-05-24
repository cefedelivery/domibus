package eu.domibus.util;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.exceptions.DomibusCoreException;
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
 * @idragusa
 * @since 4.0
 */
public class HttpUtilImplTest {

    @Tested
    HttpUtilImpl httpUtil;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void testUseProxyFalse() {
        Assert.assertFalse(httpUtil.useProxy());
    }

    @Test(expected = DomibusCoreException.class)
    public void testUseProxyException() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_ENABLED, "false");
            result = true;
        }};

        // exception is expected
        httpUtil.useProxy();
    }

    @Test
    public void testUseProxytrue() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_ENABLED, "false");
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
        Assert.assertTrue(httpUtil.useProxy());
    }

    @Test
    public void testGetConfiguredProxy() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_ENABLED, "false");
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
        HttpHost httpHost = httpUtil.getConfiguredProxy();

        Assert.assertEquals(httpHost.getPort(), 8280);
    }

    @Test
    public void testGetConfiguredCredentialsProvider() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_ENABLED, "false");
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
        CredentialsProvider credentialsProvider = httpUtil.getConfiguredCredentialsProvider();
        Assert.assertEquals("someuser", credentialsProvider.getCredentials(AuthScope.ANY).getUserPrincipal().getName());
    }
}
