package eu.domibus.api.configuration;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DomibusConfigurationService {

    String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";

    String DOMIBUS_PROXY_ENABLED = "domibus.proxy.enabled";
    String DOMIBUS_PROXY_HTTP_HOST = "domibus.proxy.http.host";
    String DOMIBUS_PROXY_HTTP_PORT = "domibus.proxy.http.port";
    String DOMIBUS_PROXY_USER = "domibus.proxy.user";
    String DOMIBUS_PROXY_PASSWORD = "domibus.proxy.password"; //NOSONAR: This is not a hardcoded password, it is just the name of a property
    String DOMIBUS_PROXY_NON_PROXY_HOSTS = "domibus.proxy.nonProxyHosts";


    String getConfigLocation();

    DataBaseEngine getDataBaseEngine();

    boolean isMultiTenantAware();
}
