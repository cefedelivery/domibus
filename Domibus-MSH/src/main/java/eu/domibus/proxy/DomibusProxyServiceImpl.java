package eu.domibus.proxy;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author idragusa
 * @since 4.1
 *
 * Loads the proxy configuration when first used
 * Domibus uses the same proxy configuration for requests on all domains
 */
@Service("domibusProxyService")
public class DomibusProxyServiceImpl implements DomibusProxyService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusProxyServiceImpl.class);

    public static final String DOMIBUS_PROXY_ENABLED = "domibus.proxy.enabled";
    public static final String DOMIBUS_PROXY_HTTP_HOST = "domibus.proxy.http.host";
    public static final String DOMIBUS_PROXY_HTTP_PORT = "domibus.proxy.http.port";
    public static final String DOMIBUS_PROXY_USER = "domibus.proxy.user";
    public static final String DOMIBUS_PROXY_PASSWORD = "domibus.proxy.password"; //NOSONAR: This is not a hardcoded password, it is just the name of a property
    public static final String DOMIBUS_PROXY_NON_PROXY_HOSTS = "domibus.proxy.nonProxyHosts";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    private DomibusProxy domibusProxy = null;
    private volatile Object domibusProxyInitLock = new Object();

    @Override
    public DomibusProxy getDomibusProxy() {
        if(domibusProxy == null) {
            synchronized (domibusProxyInitLock) {
                if(domibusProxy == null) {
                    initDomibusProxy();
                }
            }
        }
        return domibusProxy;
    }

    @Override
    public Boolean useProxy() {
        return getDomibusProxy().isEnabled();
    }

    @Override
    public Boolean isProxyUserSet() {
        return !StringUtils.isBlank(getDomibusProxy().getHttpProxyUser());
    }

    @Override
    public Boolean isNonProxyHostsSet() {
        return !StringUtils.isBlank(getDomibusProxy().getNonProxyHosts());
    }

    protected void initDomibusProxy() {
        domibusProxy = new DomibusProxy();
        LOG.info("Initialize Domibus proxy.");
        Boolean enabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROXY_ENABLED);
        if (!enabled) {
            LOG.info("Proxy not required. The property domibus.proxy.enabled is not configured");
            return;
        }
        domibusProxy.setEnabled(true);

        String httpProxyHost = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_HTTP_HOST);
        Integer httpProxyPort = domibusPropertyProvider.getIntegerProperty(DOMIBUS_PROXY_HTTP_PORT);
        if (StringUtils.isEmpty(httpProxyHost) || httpProxyPort == 0) {
            LOG.error("Proxy is enabled but the configuration is invalid: host = [{}] port = [{}]", httpProxyHost, httpProxyPort);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_006, "Proxy is enabled but the configuration is invalid.");
        }
        domibusProxy.setHttpProxyHost(httpProxyHost);
        domibusProxy.setHttpProxyPort(httpProxyPort);

        String httpProxyUser = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_USER);
        String httpProxyPassword = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_PASSWORD);
        if(!StringUtils.isEmpty(httpProxyUser) && StringUtils.isEmpty(httpProxyPassword)) {
            LOG.error("Proxy user is provided with no password [{}]", httpProxyUser);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_006, "Proxy user is provided with no password.");
        }
        domibusProxy.setHttpProxyUser(httpProxyUser);
        domibusProxy.setHttpProxyPassword(httpProxyPassword);
        String nonProxyHosts = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_NON_PROXY_HOSTS);
        domibusProxy.setNonProxyHosts(nonProxyHosts);

        LOG.info("Proxy configured: [{}]  [{}]  [{}] [{}]", httpProxyHost, httpProxyPort, httpProxyUser, nonProxyHosts);
    }
}
