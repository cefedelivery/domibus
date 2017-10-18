package eu.domibus.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hykiukira on 04/10/2017.
 */

@EnableTransactionManagement
@Profile("ORACLE_DATABASE")
public class OracleDataBaseConfig extends AbstractDatabaseConfig{

    @Override
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        driverManagerDataSource.setUrl("jdbc:oracle:thin:@localhost:1521:orcl");
        driverManagerDataSource.setUsername("edelivery");
        driverManagerDataSource.setPassword("EDELIVERY");
        return driverManagerDataSource;
    }

    @Override
    Map<Object, String> getProperties() {
        Map<Object, String> properties = new HashMap<>();
        properties.put("hibernate.dialect","org.hibernate.dialect.Oracle10gDialect");
        return properties;
    }


}
