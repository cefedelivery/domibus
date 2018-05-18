package eu.domibus.configuration;

import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
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

    private static final String DATABASE_DIALECT = "domibus.entityManagerFactory.jpaProperty.hibernate.dialect";

    private static final String MYSQL = "mysql";

    private static final String ORACLE = "oracle";

    private static final String H_2 = "h2";

    private DataBaseEngine dataBaseEngine;

    @Autowired
    @Qualifier("domibusProperties")
    private java.util.Properties domibusProperties;

    @Override
    public String getConfigLocation() {
        return System.getProperty(DOMIBUS_CONFIG_LOCATION);
    }

    @Override
    //@TODO add cacheAble here.
    public DataBaseEngine getDataBaseEngine() {
        if (dataBaseEngine == null) {
            final String property = domibusProperties.getProperty(DATABASE_DIALECT);
            if (property == null) {
                throw new IllegalStateException("Database dialect not configured, please set property: domibus.entityManagerFactory.jpaProperty.hibernate.dialect");
            }
            if (StringUtils.containsIgnoreCase(property, MYSQL)) {
                dataBaseEngine = DataBaseEngine.MYSQL;
            } else if (StringUtils.containsIgnoreCase(property, ORACLE)) {
                dataBaseEngine = DataBaseEngine.ORACLE;
            } else if (StringUtils.containsIgnoreCase(property, H_2)) {
                dataBaseEngine = DataBaseEngine.H2;
            } else {
                throw new IllegalStateException("Unsupported database dialect:" + property);
            }
        }
        return dataBaseEngine;
    }
}
