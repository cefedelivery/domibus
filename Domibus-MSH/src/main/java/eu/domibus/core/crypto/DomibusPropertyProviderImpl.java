package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.PropertyResolver;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomibusPropertyProviderImpl implements DomibusPropertyProvider {

    @Autowired
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Autowired
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

    @Autowired
    protected PropertyResolver propertyResolver;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyProviderImpl.class);

    protected String getPropertyName(Domain domain, String propertyName) {
        return domain.getCode() + "." + propertyName;
    }

    @Override
    public String getProperty(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        String propertyValue = getPropertyValue(domainPropertyName);
        if (StringUtils.isEmpty(propertyValue) && DomainService.DEFAULT_DOMAIN.equals(domain)) {
            propertyValue = getPropertyValue(propertyName);
        }
        return propertyValue;
    }

    /**
     * Get the value from the system properties; if not found get the value from Domibus properties; if still not found, look inside the Domibus default properties.
     *
     * @param propertyName the property name
     * @return The value of the property as found in the system properties, the Domibus properties or inside the default Domibus properties.
     */
    protected String getPropertyValue(String propertyName) {
        String result = System.getenv(propertyName);
        if (StringUtils.isEmpty(result)) {
            result = domibusProperties.getProperty(propertyName);

            // There is no need to retrieve the default Domibus property value here since the Domibus properties above will contain it, unless overwritten by users.
            // For String property values, if users have overwritten their original default Domibus property values, it is their responsibility to ensure they are valid.
            // For all the other Boolean and Integer property values, if users have overwritten their original default Domibus property values, they are defaulted back to their
            // original default Domibus values when invalid (please check the #getInteger..(..) and #getBoolean..(..) methods below).
        }
        return result;
    }


    @Override
    public String getProperty(Domain domain, String propertyName, String defaultValue) {
        String propertyValue = getProperty(domain, propertyName);
        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = defaultValue;
        }
        return propertyValue;
    }

    @Override
    public String getProperty(String propertyName) {
        return getProperty(DomainService.DEFAULT_DOMAIN, propertyName);
    }

    @Override
    public String getResolvedProperty(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        String resolvedProperty = propertyResolver.getResolvedProperty(domainPropertyName, domibusProperties, true);
        if (StringUtils.isEmpty(resolvedProperty) && DomainService.DEFAULT_DOMAIN.equals(domain)) {
            resolvedProperty = propertyResolver.getResolvedProperty(propertyName, domibusProperties, true);
        }
        return resolvedProperty;
    }

    @Override
    public String getResolvedProperty(String propertyName) {
        return getResolvedProperty(DomainService.DEFAULT_DOMAIN, propertyName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDomainProperty(String propertyName) {
        final Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        assert currentDomain != null;
        return getDomainProperty(currentDomain, propertyName);
    }

    @Override
    public String getDomainProperty(String propertyName, String defaultValue) {
        String propertyValue = getDomainProperty(propertyName);
        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = defaultValue;
        }
        return propertyValue;
    }

    @Override
    public String getOptionalDomainProperty(String propertyName) {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if (currentDomain == null) {
            currentDomain = DomainService.DEFAULT_DOMAIN;
        }
        return getDomainProperty(currentDomain, propertyName);
    }

    @Override
    public String getOptionalDomainProperty(final String propertyName, final String defaultValue) {
        final String propertyValue = getOptionalDomainProperty(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            return propertyValue;
        }
        return defaultValue;
    }

    @Override
    public String getDomainProperty(Domain domain, String propertyName) {
        String propertyValue = getProperty(domain, propertyName);
        if (StringUtils.isEmpty(propertyValue) && !DomainService.DEFAULT_DOMAIN.equals(domain)) {
            propertyValue = getProperty(DomainService.DEFAULT_DOMAIN, propertyName);
        }
        return propertyValue;
    }

    @Override
    public String getDomainProperty(Domain domain, String propertyName, String defaultValue) {
        String propertyValue = getDomainProperty(domain, propertyName);
        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = defaultValue;
        }
        return propertyValue;
    }

    @Override
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        Set<String> filteredPropertyNames = new HashSet<>();
        final Enumeration<?> enumeration = domibusProperties.propertyNames();
        while (enumeration.hasMoreElements()) {
            final String propertyName = (String) enumeration.nextElement();
            if (predicate.test(propertyName)) {
                filteredPropertyNames.add(propertyName);
            }
        }
        return filteredPropertyNames;
    }

    @Override
    public Integer getIntegerProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getIntegerInternal(propertyName, value);
    }

    @Override
    public Integer getIntegerDomainProperty(String propertyName) {
        String domainValue = getDomainProperty(propertyName);
        return getIntegerInternal(propertyName, domainValue);
    }

    @Override
    public Integer getIntegerDomainProperty(Domain domain, String propertyName) {
        String domainValue = getDomainProperty(domain, propertyName);
        return getIntegerInternal(propertyName, domainValue);
    }

    @Override
    public Integer getIntegerOptionalDomainProperty(String propertyName) {
        String optionalDomainValue = getOptionalDomainProperty(propertyName);
        return getIntegerInternal(propertyName, optionalDomainValue);
    }

    private Integer getIntegerInternal(String propertyName, String customValue) {
        if (customValue != null) {
            try {
                return Integer.valueOf(customValue);
            } catch (final NumberFormatException e) {
                LOGGER.warn("Could not parse the property [" + propertyName + "] custom value [" + customValue + "] to an integer value", e);
                return getDefaultIntegerValue(propertyName);
            }
        }
        return getDefaultIntegerValue(propertyName);
    }

    private Integer getDefaultIntegerValue(String propertyName) {
        Integer defaultValue = MapUtils.getInteger(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getBooleanInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanDomainProperty(String propertyName) {
        String domainValue = getDomainProperty(propertyName);
        return getBooleanInternal(propertyName, domainValue);
    }

    @Override
    public Boolean getBooleanDomainProperty(Domain domain, String propertyName) {
        String domainValue = getDomainProperty(domain, propertyName);
        return getBooleanInternal(propertyName, domainValue);
    }

    @Override
    public Boolean getBooleanOptionalDomainProperty(String propertyName) {
        String optionalDomainValue = getOptionalDomainProperty(propertyName);
        return getBooleanInternal(propertyName, optionalDomainValue);
    }

    @Override
    public boolean containsDomainPropertyKey(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        return domibusProperties.containsKey(domainPropertyName);
    }

    private Boolean getBooleanInternal(String propertyName, String customValue) {
        if (customValue != null) {
            Boolean customBoolean = BooleanUtils.toBooleanObject(customValue);
            if (customBoolean != null) {
                return customBoolean;
            }
            LOGGER.warn("Could not parse the property [{}] custom value [{}] to a boolean value", propertyName, customValue);
            return getDefaultBooleanValue(propertyName);
        }
        return getDefaultBooleanValue(propertyName);
    }

    private Boolean getDefaultBooleanValue(String propertyName) {
        // We need to fetch the Boolean value in two steps as the MapUtils#getBoolean(Properties, String) does not return "null" when the value is an invalid Boolean.
        String defaultValue = MapUtils.getString(domibusDefaultProperties, propertyName);
        Boolean defaultBooleanValue = BooleanUtils.toBooleanObject(defaultValue);
        return checkDefaultValue(propertyName, defaultBooleanValue);
    }

    private <T> T checkDefaultValue(String propertyName, T defaultValue) {
        if (defaultValue == null) {
            throw new IllegalStateException("The default property [" + propertyName + "] is required but was either not found inside the default properties or found having an invalid value");
        }
        LOGGER.debug("Found the property [{}] default value [{}]", propertyName, defaultValue);
        return defaultValue;
    }


}
