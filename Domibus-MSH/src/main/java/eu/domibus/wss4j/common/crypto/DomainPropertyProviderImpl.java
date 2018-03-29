package eu.domibus.wss4j.common.crypto;

import eu.domibus.property.PropertyResolver;
import eu.domibus.wss4j.common.crypto.api.DomainPropertyProvider;
import org.apache.commons.lang3.StringUtils;
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
    public String getPropertyName(String domain, String propertyName) {
        String prefix = "";
        if (StringUtils.isNotEmpty(domain)) {
            prefix = domain + ".";
        }
        return prefix + propertyName;
    }

    @Override
    public String getPropertyValue(String domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        return domibusProperties.getProperty(domainPropertyName);
    }

    @Override
    public String getResolvedPropertyValue(String domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        return propertyResolver.getResolvedProperty(domainPropertyName, domibusProperties, true);
    }
}
