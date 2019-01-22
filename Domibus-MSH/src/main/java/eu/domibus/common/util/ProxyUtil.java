package eu.domibus.common.util;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.proxy.DomibusProxy;
import org.apache.commons.lang3.StringUtils;
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
    DomibusProxy domibusProxy;

    public Boolean useProxy() {
        return domibusProxy.isEnabled();
    }

    public HttpHost getConfiguredProxy() {
        if (domibusProxy.isEnabled()) {
            LOG.debug("Proxy enabled, get configured proxy [{}] [{}]", domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort());
            return new HttpHost(domibusProxy.getHttpProxyHost(), Integer.parseInt(domibusProxy.getHttpProxyPort()));
        }
        LOG.debug("Proxy not enabled, configured proxy is null");
        return null;
    }

    public CredentialsProvider getConfiguredCredentialsProvider() {
        if(domibusProxy.isEnabled() && !StringUtils.isBlank(domibusProxy.getHttpProxyUser())) {
            LOG.debug("Proxy enabled, configure credentials provider for [{}]", domibusProxy.getHttpProxyUser());
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(domibusProxy.getHttpProxyHost(), Integer.parseInt(domibusProxy.getHttpProxyPort())),
                    new UsernamePasswordCredentials(domibusProxy.getHttpProxyUser(), domibusProxy.getHttpProxyPassword()));

            return credsProvider;
        }
        LOG.debug("Proxy not enabled, credentials provider is null");
        return null;
    }
}
