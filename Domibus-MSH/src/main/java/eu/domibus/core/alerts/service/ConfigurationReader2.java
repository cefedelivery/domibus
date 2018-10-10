package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@FunctionalInterface
public interface ConfigurationReader2<E> {

    E readConfiguration(String property, String title, Domain domain);

}
