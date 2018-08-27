package eu.domibus.configuration.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import java.util.Optional;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Configuration
public class StorageConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StorageConfiguration.class);

    @Bean(name = "storage")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Storage storage(Domain domain) {

        Storage storage = new Storage();
        storage.setDomain(domain);

        return  storage;
    }

}
