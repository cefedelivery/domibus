package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.property.PropertyResolver;
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
    protected PropertyResolver propertyResolver;

    @Autowired
    protected DomainContextProvider domainContextProvider;

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
     * Get the value from the system properties and if not found get the value from domibus properties
     *
     * @param propertyName
     * @return
     */
    protected String getPropertyValue(String propertyName) {
        String result = System.getenv(propertyName);
        if (StringUtils.isEmpty(result)) {
            result = domibusProperties.getProperty(propertyName);
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
    public String getProperty(String propertyName, String defaultValue) {
        String propertyValue = getProperty(propertyName);
        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = defaultValue;
        }
        return propertyValue;
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
        if(currentDomain==null){
            currentDomain=DomainService.DEFAULT_DOMAIN;
        }
        return getDomainProperty(currentDomain,propertyName);
    }

    @Override
    public String getOptionalDomainProperty(final String propertyName,final String defaultValue) {
        final String propertyValue = getOptionalDomainProperty(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            return  propertyValue;
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
}
