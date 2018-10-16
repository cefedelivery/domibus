package eu.domibus.core.alerts.model.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.alerts.service.ConfigurationReader;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
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

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConfigurationLoader.class);

    @Autowired
    private DomainContextProvider domainContextProvider;

    private final Map<Domain, E> configuration = new HashMap<>();

    public E getConfiguration(ConfigurationReader<E> configurationReader) {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        final Domain domain = currentDomain ==null?DomainService.DEFAULT_DOMAIN:currentDomain;
        LOG.debug("Retrieving alert messaging configuration for domain:[{}]", domain);
        if (this.configuration.get(domain) == null) {
            synchronized (this.configuration) {
                if (this.configuration.get(domain) == null) {
                    this.configuration.computeIfAbsent(domain, configurationReader::readConfiguration);
                }
            }
        }
        E result = this.configuration.get(domain);
        LOG.debug("Alert messaging configuration:[{}]", result);
        return result;

    }

}



