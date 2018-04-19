package eu.domibus.api.property;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomibusPropertyProvider {

    String getPropertyValue(String propertyName);

    String getPropertyValue(String propertyName, String defaultValue);

    String getPropertyValue(Domain domain, String propertyName);

    String getPropertyValue(Domain domain, String propertyName, String defaultValue);

    String getResolvedPropertyValue(Domain domain, String propertyName);
}
