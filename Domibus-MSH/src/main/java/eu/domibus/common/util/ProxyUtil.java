package eu.domibus.common.util;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * @author idragusa
 * @since 5/22/18.
 */
@Component
public class ProxyUtil {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ProxyUtil.class);

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    public HttpHost getConfiguredProxy() {
        if (domibusConfigurationService.useProxy()) {
            String httpProxyHost = domibusProperties.getProperty(domibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusProperties.getProperty(domibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);

            return new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort));
        }
        return null;
    }

    public CredentialsProvider getConfiguredCredentialsProvider() {
        if(domibusConfigurationService.useProxy()) {
            String httpProxyHost = domibusProperties.getProperty(domibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusProperties.getProperty(domibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            String httpProxyUser = domibusProperties.getProperty(domibusConfigurationService.DOMIBUS_PROXY_USER);
            String httpProxyPassword = domibusProperties.getProperty(domibusConfigurationService.DOMIBUS_PROXY_PASSWORD);

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(httpProxyHost, Integer.parseInt(httpProxyPort)),
                    new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword));

            return credsProvider;
        }
        return null;
    }
}
