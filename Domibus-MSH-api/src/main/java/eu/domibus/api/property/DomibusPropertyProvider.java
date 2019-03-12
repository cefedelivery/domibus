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

    String getProperty(Domain domain, String propertyName);

    String getProperty(Domain domain, String propertyName, String defaultValue);

    String getResolvedProperty(Domain domain, String propertyName);

    String getResolvedProperty(String propertyName);


    /*
    The getDomainProperty methods retrieve the specified property
    falling back to the property from the DEFAULT domain if not found.
     */

    /**
     * Look for a property in the active domain configuration file. If the property is not found, it will search for the property in
     * the following locations and in the respective order:
     * conf/domibus.properties, classpath://domibus.properties, classpath://domibus-default.properties
     *
     * @param propertyName the property name.
     * @return the value for that property.
     */
    String getDomainProperty(String propertyName);

    String getDomainProperty(String propertyName, String defaultValue);

    /**
     * When actions are executed under a super admin user, there is no domain set on the current thread.
     * Nevertheless we need to retrieve some default properties. So if no domain is found, this method will retrieve
     * properties from the default one.
     * @param propertyName the property name.
     * @return the property value.
     */
    String getOptionalDomainProperty(String propertyName);

    String getOptionalDomainProperty(String propertyName, String defaultValue);

    String getDomainProperty(Domain domain, String propertyName);

    String getDomainProperty(Domain domain, String propertyName, String defaultValue);

    Set<String> filterPropertiesName(Predicate<String> predicate);

    /**
     * <p>Reads a property value inside the {@link eu.domibus.api.multitenancy.DomainService#DEFAULT_DOMAIN DEFAULT} domain and parses it safely as an {@code Integer} before
     * returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     *
     * @return The {@code Integer} value of the property as specified by the user or the default one provided with the application.
     */
    Integer getIntegerProperty(String propertyName);

    /**
     * <p>Reads a domain property value and parses it safely as an {@code Integer} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     *
     * @return The {@code Integer} value of the domain property as specified by the user or the default one provided with the application.
     */
    Integer getIntegerDomainProperty(String propertyName);

    Integer getIntegerDomainProperty(Domain domain, String propertyName);

    Long getLongDomainProperty(Domain domain, String propertyName);

    /**
     * <p>Reads an optional domain property value and parses it safely as an {@code Integer} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     *
     * @return The {@code Integer} value of the optional domain property as specified by the user or the default one provided with the application.
     */
    Integer getIntegerOptionalDomainProperty(String propertyName);

    /**
     * <p>Reads a property value inside the {@link eu.domibus.api.multitenancy.DomainService#DEFAULT_DOMAIN DEFAULT} domain and parses it safely as a {@code Boolean} before
     * returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     *
     * @return The {@code Boolean} value of the property as specified by the user or the default one provided with the application.
     */
    Boolean getBooleanProperty(String propertyName);

    /**
     * <p>Reads a domain property value and parses it safely as a {@code Boolean} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     *
     * @return The {@code Boolean} value of the domain property as specified by the user or the default one provided with the application.
     */
    Boolean getBooleanDomainProperty(String propertyName);

    Boolean getBooleanDomainProperty(Domain domain, String propertyName);

    /**
     * <p>Reads an optional domain property value and parses it safely as a {@code Boolean} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     *
     * @return The {@code Boolean} value of the optional domain property as specified by the user or the default one provided with the application.
     */
    Boolean getBooleanOptionalDomainProperty(String propertyName);

}
