package eu.domibus.configuration;

import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
}
