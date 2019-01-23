package eu.domibus.proxy;

/**
 * @author idragusa
 * @since 4.1
 */
public interface DomibusProxyService {

    /*
    * Get the object containing the configured proxy properties
    * */
    DomibusProxy getDomibusProxy();

    /*
    * Check if proxy usage is required
    * */
    Boolean useProxy();

    /*
    * Proxy user is only set when the proxy requires basic authentication
    * */
    Boolean isProxyUserSet();
}
