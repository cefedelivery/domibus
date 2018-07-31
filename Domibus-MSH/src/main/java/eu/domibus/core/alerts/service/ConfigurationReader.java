package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@FunctionalInterface
public interface ConfigurationReader<E> {

    E readConfiguration(Domain domain);

}
