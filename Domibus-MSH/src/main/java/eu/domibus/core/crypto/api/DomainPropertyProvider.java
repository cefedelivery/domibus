package eu.domibus.core.crypto.api;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainPropertyProvider {

    String getPropertyName(Domain domain, String propertyName);

    String getPropertyValue(Domain domain, String propertyName);

    String getResolvedPropertyValue(Domain domain, String propertyName);
}
