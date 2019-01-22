package eu.domibus.proxy;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import junit.framework.Assert;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author idragusa
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DomibusProxyTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusProxyTest.class);

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    DomibusProxy domibusProxy;

    @Test(expected = DomibusCoreException.class)
    public void initDomibusProxyMissingHostTest() {
        new NonStrictExpectations(){{
            domibusPropertyProvider.getBooleanProperty(DomibusProxy.DOMIBUS_PROXY_ENABLED);
            result = true;

        }};
        domibusProxy.initDomibusProxy();
    }

    @Test(expected = DomibusCoreException.class)
    public void initDomibusProxyMissingPortTest() {
        new NonStrictExpectations(){{
            domibusPropertyProvider.getBooleanProperty(DomibusProxy.DOMIBUS_PROXY_ENABLED);
            result = true;

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_HTTP_HOST);
            result = "12.13.14.15";

        }};
        domibusProxy.initDomibusProxy();
    }

    @Test
    public void initDomibusProxyTest() {
        new NonStrictExpectations(){{
            domibusPropertyProvider.getBooleanProperty(DomibusProxy.DOMIBUS_PROXY_ENABLED);
            result = true;

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_HTTP_HOST);
            result = "12.13.14.15";

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_HTTP_PORT);
            result = "8012";

        }};
        domibusProxy.initDomibusProxy();
    }

    @Test(expected = DomibusCoreException.class)
    public void initDomibusProxyMissingPasswordTest() {
        new NonStrictExpectations(){{
            domibusPropertyProvider.getBooleanProperty(DomibusProxy.DOMIBUS_PROXY_ENABLED);
            result = true;

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_HTTP_HOST);
            result = "12.13.14.15";

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_HTTP_PORT);
            result = "8012";

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_USER);
            result = "idragusa";

        }};
        domibusProxy.initDomibusProxy();
    }

    @Test
    public void initDomibusProxyAuthTest() {
        new NonStrictExpectations(){{
            domibusPropertyProvider.getBooleanProperty(DomibusProxy.DOMIBUS_PROXY_ENABLED);
            result = true;

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_HTTP_HOST);
            result = "12.13.14.15";

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_HTTP_PORT);
            result = "8012";

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_USER);
            result = "idragusa";

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_PASSWORD);
            result = "pass";

            domibusPropertyProvider.getProperty(DomibusProxy.DOMIBUS_PROXY_NON_PROXY_HOSTS);
            result = "localhost";


        }};
        domibusProxy.initDomibusProxy();
        Assert.assertTrue(domibusProxy.isEnabled());
        Assert.assertEquals("12.13.14.15", domibusProxy.getHttpProxyHost());
        Assert.assertEquals("8012", domibusProxy.getHttpProxyPort());
        Assert.assertEquals("idragusa", domibusProxy.getHttpProxyUser());
        Assert.assertEquals("pass", domibusProxy.getHttpProxyPassword());
        Assert.assertEquals("localhost", domibusProxy.getNonProxyHosts());

    }
}


