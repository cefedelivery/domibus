package eu.domibus.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@EnableTransactionManagement
@Profile("IN_MEMORY_DATABASE")
public class InMemoryDataBaseConfig extends AbstractDatabaseConfig{


    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();

    }


    Map<Object, String> getProperties() {
        return new HashMap<>();
    }

}
