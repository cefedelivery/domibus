package eu.domibus.api.configuration;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DomibusConfigurationService {

    String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";

    String FOURCORNERMODEL_ENABLED_KEY = "domibus.fourcornermodel.enabled";
    String CLUSTER_DEPLOYMENT = "domibus.deployment.clustered";

    String getConfigLocation();

    boolean isClusterDeployment();

    DataBaseEngine getDataBaseEngine();

    boolean isMultiTenantAware();

    boolean isFourCornerEnabled();

}
