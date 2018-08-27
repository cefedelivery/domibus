package eu.domibus.core.alerts.dao;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertDaoConfig {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(AlertDaoConfig.class);

    @Bean
    public AlertDao alertDao(){
        return new AlertDao();
    }

    @Bean
    public EventDao eventDao(){
        return new EventDao();
    }

}
