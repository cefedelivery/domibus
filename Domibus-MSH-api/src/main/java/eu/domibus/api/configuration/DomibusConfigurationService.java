package eu.domibus.api.configuration;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DomibusConfigurationService {

    String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";

    String FOURCORNERMODEL_ENABLED_KEY = "domibus.fourcornermodel.enabled";
    String CLUSTER_DEPLOYMENT = "domibus.deployment.clustered";
    String EXTERNAL_AUTH_PROVIDER = "domibus.security.ext.auth.provider.enabled";

    String getConfigLocation();

    boolean isClusterDeployment();

    DataBaseEngine getDataBaseEngine();

    boolean isMultiTenantAware();

    boolean isFourCornerEnabled();

    /**
     * Returns true if external authentication provider is enabled
     *
     * @return boolean - true if an authentication external provider is enabled
     */
    boolean isExtAuthProviderEnabled();

}
