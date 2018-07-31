package eu.domibus.common.util;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author idragusa
 */
@Component
public class ProxyUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ProxyUtil.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    public HttpHost getConfiguredProxy() {
        if (domibusConfigurationService.useProxy()) {
            String httpProxyHost = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);

            return new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort));
        }
        return null;
    }

    public CredentialsProvider getConfiguredCredentialsProvider() {
        if(domibusConfigurationService.useProxy()) {
            String httpProxyHost = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            String httpProxyUser = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            String httpProxyPassword = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(httpProxyHost, Integer.parseInt(httpProxyPort)),
                    new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword));

            return credsProvider;
        }
        return null;
    }

}
