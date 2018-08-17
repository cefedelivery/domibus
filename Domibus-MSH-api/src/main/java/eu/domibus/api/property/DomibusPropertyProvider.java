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


    /*
    The getDomainProperty methods retrieve the specified property
    falling back to the property from the DEFAULT domain if not found.
     */

    /**
     * Look for a property in the current domain. If the property is not found, it will search for the property in
     * the following locations and in the respective order:
     * in conf/domibus.properties, classpath://domibus.properties, classpath://domibus-default.properties
     * @param propertyName the property name.
     * @return the value for that property.
     */
    String getDomainProperty(String propertyName);

    String getDomainProperty(String propertyName, String defaultValue);

    String getDomainProperty(Domain domain, String propertyName);

    String getDomainProperty(Domain domain, String propertyName, String defaultValue);

    Set<String> filterPropertiesName(Predicate<String> predicate);
}
