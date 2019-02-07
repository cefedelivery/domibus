package eu.domibus.proxy;


import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * @author idragusa
 * @since 4.1
 *
 * Holds the domibus proxy configuration
 *
 */
public class DomibusProxy {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusProxy.class);

    protected Boolean enabled;
    protected String httpProxyHost;
    protected Integer httpProxyPort;
    protected String httpProxyUser;
    protected String httpProxyPassword;
    protected String nonProxyHosts;

    public DomibusProxy() {
        this.enabled = false;
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
