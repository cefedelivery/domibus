package eu.domibus.configuration;

import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DefaultDomibusConfigurationService implements DomibusConfigurationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultDomibusConfigurationService.class);

    private static final String DATABASE_DIALECT = "domibus.entityManagerFactory.jpaProperty.hibernate.dialect";

    private DataBaseEngine dataBaseEngine;

    @Autowired
    @Qualifier("domibusProperties")
    private java.util.Properties domibusProperties;

    @Override
    public String getConfigLocation() {
        return System.getProperty(DOMIBUS_CONFIG_LOCATION);
    }

    @Override
    public DataBaseEngine getDataBaseEngine() {
        if (dataBaseEngine == null) {
            final String property = domibusProperties.getProperty(DATABASE_DIALECT);
            if (property == null) {
                throw new IllegalStateException("Database dialect not configured, please set property: domibus.entityManagerFactory.jpaProperty.hibernate.dialect");
            }
            dataBaseEngine = DataBaseEngine.getDatabaseEngine(property);
            LOG.info("Database engine:[{}]", dataBaseEngine);
        }
        return dataBaseEngine;
    }

    @Override
    public boolean useProxy() {
        String useProxy = domibusProperties.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_ENABLED, "false");
        if (StringUtils.isEmpty(useProxy) || !Boolean.parseBoolean(useProxy)) {
            LOG.debug("Proxy not required. The property domibus.proxy.enabled is not configured");
            return false;
        }

        String httpProxyHost = domibusProperties.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
        String httpProxyPort = domibusProperties.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
        String httpProxyUser = domibusProperties.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
        String httpProxyPassword = domibusProperties.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);

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
}
