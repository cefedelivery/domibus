package eu.domibus.proxy;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author idragusa
 * @since 4.1
 *
 * Initialize and holds the proxy configuration for Domibus.
 * Domibus allows one proxy for all requests on all domains.
 *
 */
@Component
public class DomibusProxy {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusProxy.class);

    public static final String DOMIBUS_PROXY_ENABLED = "domibus.proxy.enabled";
    public static final String DOMIBUS_PROXY_HTTP_HOST = "domibus.proxy.http.host";
    public static final String DOMIBUS_PROXY_HTTP_PORT = "domibus.proxy.http.port";
    public static final String DOMIBUS_PROXY_USER = "domibus.proxy.user";
    public static final String DOMIBUS_PROXY_PASSWORD = "domibus.proxy.password"; //NOSONAR: This is not a hardcoded password, it is just the name of a property
    public static final String DOMIBUS_PROXY_NON_PROXY_HOSTS = "domibus.proxy.nonProxyHosts";

    protected Boolean enabled;
    protected String httpProxyHost;
    protected Integer httpProxyPort;
    protected String httpProxyUser;
    protected String httpProxyPassword;
    protected String nonProxyHosts;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @PostConstruct
    public void initDomibusProxy() {
        LOG.info("Initialize Domibus proxy.");
        this.enabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROXY_ENABLED);
        if (!this.enabled) {
            LOG.info("Proxy not required. The property domibus.proxy.enabled is not configured");
            return;
        }

        this.httpProxyHost = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_HTTP_HOST);
        this.httpProxyPort = domibusPropertyProvider.getIntegerProperty(DOMIBUS_PROXY_HTTP_PORT);
        if (StringUtils.isEmpty(httpProxyHost) || httpProxyPort == 0) {
            LOG.error("Proxy is enabled but the configuration is invalid: host = [{}] port = [{}]", httpProxyHost, httpProxyPort);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_006, "Proxy is enabled but the configuration is invalid.");
        }

        this.httpProxyUser = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_USER);
        this.httpProxyPassword = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_PASSWORD);
        if(!StringUtils.isEmpty(httpProxyUser) && StringUtils.isEmpty(httpProxyPassword)) {
            LOG.error("Proxy user is provided with no password [{}]", httpProxyUser);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_006, "Proxy user is provided with no password.");
        }
        this.nonProxyHosts = domibusPropertyProvider.getProperty(DOMIBUS_PROXY_NON_PROXY_HOSTS);

        LOG.info("Proxy configured: [{}]  [{}]  [{}] [{}]", httpProxyHost, httpProxyPort, httpProxyUser, nonProxyHosts);
    }

    public DomibusProxy() {
        this.enabled = null;
        this.httpProxyHost = null;
        this.httpProxyPort = null;
        this.httpProxyUser = null;
        this.httpProxyPassword = null;
        this.nonProxyHosts = null;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    public Integer getHttpProxyPort() {
        return httpProxyPort;
    }

    public void setHttpProxyPort(Integer httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    public String getHttpProxyUser() {
        return httpProxyUser;
    }

    public void setHttpProxyUser(String httpProxyUser) {
        this.httpProxyUser = httpProxyUser;
    }

    public String getHttpProxyPassword() {
        return httpProxyPassword;
    }

    public void setHttpProxyPassword(String httpProxyPassword) {
        this.httpProxyPassword = httpProxyPassword;
    }

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
