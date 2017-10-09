package eu.domibus.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Created by hykiukira on 04/10/2017.
 */
@Configuration
public abstract class AbstractDatabaseConfig {

    abstract DataSource dataSource();

    abstract Map<Object,String> getProperties();

    @Bean
    public LocalContainerEntityManagerFactoryBean em() {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setPackagesToScan("eu.domibus.common.model","eu.domibus.ebms3.common.model");
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto","update");
        jpaProperties.put("org.hibernate.envers.audit_strategy","org.hibernate.envers.strategy.ValidityAuditStrategy");
        //jpaProperties.put("org.hibernate.envers.track_entities_changed_in_revision","true");
        jpaProperties.put("org.hibernate.envers.store_data_at_delete","true");
        jpaProperties.put("org.hibernate.envers.using_modified_flag", "true");

        jpaProperties.putAll(getProperties());
        localContainerEntityManagerFactoryBean.setJpaProperties(jpaProperties);
        localContainerEntityManagerFactoryBean.setDataSource(dataSource());
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public JpaTransactionManager jpaTransactionManager(){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(em().getObject());
        return jpaTransactionManager;
    }
}
