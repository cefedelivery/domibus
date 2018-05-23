package eu.domibus.property;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Service
public class PropertyResolver {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyResolver.class);


    public String getResolvedProperty(String propertyName) {
        return getResolvedProperty(propertyName, new Properties(), true);
    }

    /**
     * Resolves a value against the system properties
     *
     * See {@link PropertyResolver#getResolvedValue(String, Properties, boolean)}
     * @param value
     * @return
     */
    public String getResolvedValue(String value) {
        return getResolvedValue(value, new Properties(), true);
    }

    /**
     *
     * Resolves a value against the provided properties
     *
     * <p>
     * Eg: <br>
     * Properties properties = new Properties(); <br>
     * properties.setProperty("wsPluginLocation", "${pluginsDirectory}/ws"); <br>
     * String resolvedProperty = propertyResolver.getResolvedProperty("wsPluginLocation", properties, false); <br>
     * resolvedProperty is /home/plugins/ws
     * </p>
     *
     * @param value The value to be resolved
     * @param properties The properties which the provided value will be resolved against
     * @param includeSystemProperties If true the system properties will be taken into account when resolving the value
     * @return The resolved value
     */
    public String getResolvedValue(String value, Properties properties, boolean includeSystemProperties) {
        String temporaryPropertyName = "_tempValue";
        Properties tempProperties = new Properties();
        tempProperties.setProperty(temporaryPropertyName , value);
        if(properties != null) {
            tempProperties.putAll(properties);
        }
        String resolvedProperty = getResolvedProperty(temporaryPropertyName, tempProperties, includeSystemProperties);
        if(StringUtils.isEmpty(resolvedProperty)) {
            LOG.debug("[{}] could not be resolved, returning the original value", value);
            resolvedProperty = value;
        }
        return resolvedProperty;
    }

    public String getResolvedProperty(String propertyName, Properties properties, boolean includeSystemProperties) {
        if(propertyName == null) {
            return null;
        }

        if (includeSystemProperties) {
            LOG.debug("Adding the system properties to the available properties");
            properties.putAll(System.getProperties());
        }

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setProperties(properties);
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("myproperties", properties);
        final MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addFirst(propertiesPropertySource);
        PropertySourcesPropertyResolver propertySourcesPropertyResolver = new PropertySourcesPropertyResolver(propertySources);

        String result = properties.getProperty(propertyName);
        try {
            result = propertySourcesPropertyResolver.getProperty(propertyName);
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not resolve property [{}]: {}", propertyName, e.getMessage());
        }
        return result;
    }
}
