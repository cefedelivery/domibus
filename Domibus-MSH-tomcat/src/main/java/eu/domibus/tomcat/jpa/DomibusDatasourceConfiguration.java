package eu.domibus.tomcat.jpa;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.spring.PrefixedProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomibusDatasourceConfiguration {

    @Autowired
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Bean(name = "domibusJDBC-XADataSource", initMethod = "init", destroyMethod = "close")
    @DependsOn("userTransactionService")
    public AtomikosDataSourceBean domibusXADatasource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("domibusJDBC-XA");
        final String xaDataSourceClassName = domibusPropertyProvider.getPropertyValue("domibus.datasource.xa.xaDataSourceClassName");
        dataSource.setXaDataSourceClassName(xaDataSourceClassName);
        dataSource.setXaProperties(xaProperties());
        final Integer minPoolSize = Integer.parseInt(domibusPropertyProvider.getPropertyValue("domibus.datasource.xa.minPoolSize", "5"));
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = Integer.parseInt(domibusPropertyProvider.getPropertyValue("domibus.datasource.xa.maxPoolSize", "100"));
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifeTime = Integer.parseInt(domibusPropertyProvider.getPropertyValue("domibus.datasource.xa.maxLifetime", "60"));
        dataSource.setMaxLifetime(maxLifeTime);

        return dataSource;
    }

    @Bean
    public PrefixedProperties xaProperties() {
        PrefixedProperties result = new PrefixedProperties(domibusProperties, "domibus.datasource.xa.property.");
        return result;
    }
}
