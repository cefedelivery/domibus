package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.crypto.api.DomainPropertyProvider;
import eu.domibus.property.PropertyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainPropertyProviderImpl implements DomainPropertyProvider {

    @Autowired
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Autowired
    PropertyResolver propertyResolver;

    @Override
    public String getPropertyName(Domain domain, String propertyName) {
        String prefix = "";
        if (!DomainService.DEFAULT_DOMAIN.equals(domain)) {
            prefix = domain.getCode() + ".";
        }
        return prefix + propertyName;
    }

    @Override
    public String getPropertyValue(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        return domibusProperties.getProperty(domainPropertyName);
    }

    @Override
    public String getResolvedPropertyValue(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        return propertyResolver.getResolvedProperty(domainPropertyName, domibusProperties, true);
    }
}
