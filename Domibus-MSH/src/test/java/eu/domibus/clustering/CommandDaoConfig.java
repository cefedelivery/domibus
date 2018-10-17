package eu.domibus.clustering;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Configuration
public class CommandDaoConfig {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(CommandDaoConfig.class);

    @Bean
    public CommandDao commandDaoDao() {
        return new CommandDao();
    }

}
