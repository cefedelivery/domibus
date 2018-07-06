package eu.domibus.core.alerts.model.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.service.ConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Component
@Scope("prototype")
public class ConfigurationLoader<E> {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationLoader.class);

    @Autowired
    private DomainContextProvider domainContextProvider;

    private final Map<Domain, E> configuration = new HashMap<>();

    public E getConfiguration(ConfigurationReader<E> configurationReader) {
        final Domain domain = domainContextProvider.getCurrentDomain();
        E messagingConfiguration = this.configuration.get(domain);
        LOG.debug("Retrieving alert messaging configuration for domain:[{}]", domain);
        if (messagingConfiguration == null) {
            synchronized (configuration) {
                messagingConfiguration = this.configuration.get(domain);
                if (messagingConfiguration == null) {
                    messagingConfiguration = configurationReader.readConfiguration(domain);
                    configuration.put(domain, messagingConfiguration);
                }
            }
        }
        messagingConfiguration = configuration.get(domain);
        LOG.debug("Alert messaging configuration:[{}]", messagingConfiguration);
        return messagingConfiguration;

    }

}



