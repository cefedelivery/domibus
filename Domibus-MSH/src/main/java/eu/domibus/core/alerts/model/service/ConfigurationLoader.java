package eu.domibus.core.alerts.model.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.service.ConfigurationReader;
import eu.domibus.logging.DomibusLoggerFactory;
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

    private final static Logger LOG = DomibusLoggerFactory.getLogger(ConfigurationLoader.class);

    @Autowired
    private DomainContextProvider domainContextProvider;

    private final Map<Domain, E> configuration = new HashMap<>();

    public E getConfiguration(ConfigurationReader<E> configurationReader) {
        final Domain domain = domainContextProvider.getCurrentDomain();
        E configuration = this.configuration.get(domain);
        LOG.debug("Retrieving alert messaging configuration for domain:[{}]", domain);
        if (configuration == null) {
            synchronized (this.configuration) {
                this.configuration.computeIfAbsent(domain, configurationReader::readConfiguration);
            }
        }
        configuration = this.configuration.get(domain);
        LOG.debug("Alert messaging configuration:[{}]", configuration);
        return configuration;

    }

}



