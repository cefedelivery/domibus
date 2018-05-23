package eu.domibus.common.util;

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

    public boolean useProxy() {
        String useProxy = domibusProperties.getProperty("domibus.proxy.enabled", "false");
        if (StringUtils.isEmpty(useProxy)) {
            LOG.debug("Proxy not required. The property domibus.proxy.enabled is not configured");
            return false;
        }

        String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
        String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");
        String httpProxyUser = domibusProperties.getProperty("domibus.proxy.user");
        String httpProxyPassword = domibusProperties.getProperty("domibus.proxy.password");

        if (StringUtils.isEmpty(httpProxyHost) || StringUtils.isEmpty(httpProxyPort)
                || StringUtils.isEmpty(httpProxyUser) || StringUtils.isEmpty(httpProxyPassword)) {

            LOG.error("Proxy is enabled but the configuration is invalid:" + httpProxyHost + " " + httpProxyPort + " " +
                    httpProxyUser);

            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_006, "Proxy is enabled but the configuration is invalid.");
        }
        LOG.info("Proxy configured: " + httpProxyHost + " " + httpProxyPort + " " +
                httpProxyUser);

        return Boolean.parseBoolean(useProxy);
    }


    protected HttpHost getConfiguredProxy() {
        if (useProxy()) {
            String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
            String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");

            return new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort));
        }
        return null;
    }

    protected CredentialsProvider getConfiguredCredentialsProvider() {
        if(useProxy()) {
            String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
            String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");
            String httpProxyUser = domibusProperties.getProperty("domibus.proxy.user");
            String httpProxyPassword = domibusProperties.getProperty("domibus.proxy.password");

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(httpProxyHost, Integer.parseInt(httpProxyPort)),
                    new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword));

            return credsProvider;
        }
        return null;
    }
}
