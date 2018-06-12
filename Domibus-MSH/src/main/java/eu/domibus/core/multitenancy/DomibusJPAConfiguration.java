package eu.domibus.core.multitenancy;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.spring.PrefixedProperties;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomibusJPAConfiguration {

    @Autowired
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Qualifier("domibusJDBC-XADataSource")
    @Autowired
    protected DataSource dataSource;

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @Primary
    @DependsOn("transactionManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(Optional<MultiTenantConnectionProvider> multiTenantConnectionProviderImpl,
                                                                       Optional<CurrentTenantIdentifierResolver> tenantIdentifierResolver)  {
        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setPersistenceUnitName("domibusJTA");
        final String packagesToScanString = domibusPropertyProvider.getProperty("domibus.entityManagerFactory.packagesToScan");
        if(StringUtils.isNotEmpty(packagesToScanString)) {
            final String[] packagesToScan = StringUtils.split(packagesToScanString, ",");
            result.setPackagesToScan(packagesToScan);
        }
        result.setJtaDataSource(dataSource);
        result.setJpaVendorAdapter(jpaVendorAdapter());
        final PrefixedProperties jpaProperties = jpaProperties();

        final boolean tenantConnectionProviderPresent = multiTenantConnectionProviderImpl.isPresent();
        if(tenantConnectionProviderPresent) {
            jpaProperties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
            jpaProperties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl.get());
            jpaProperties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver.get());
        }
        result.setJpaProperties(jpaProperties);

        return result;
    }

    @Bean
    public PrefixedProperties jpaProperties() {
        PrefixedProperties result = new PrefixedProperties(domibusProperties, "domibus.entityManagerFactory.jpaProperty.");
        return result;
    }
}
