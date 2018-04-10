package eu.domibus.core.crypto.api;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainPropertyProvider {

    String getPropertyValue(String propertyName);

    String getPropertyValue(String propertyName, String defaultValue);

    String getPropertyValue(Domain domain, String propertyName);

    String getPropertyValue(Domain domain, String propertyName, String defaultValue);

    String getResolvedPropertyValue(Domain domain, String propertyName);
}
