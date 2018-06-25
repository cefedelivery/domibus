package eu.domibus.api.property;

import eu.domibus.api.multitenancy.Domain;

import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomibusPropertyProvider {

    String getProperty(String propertyName);

    String getProperty(String propertyName, String defaultValue);

    String getProperty(Domain domain, String propertyName);

    String getProperty(Domain domain, String propertyName, String defaultValue);

    String getResolvedProperty(Domain domain, String propertyName);

    String getResolvedProperty(String propertyName);

    Set<String> filterPropertiesName(Predicate<String> predicate);
}
