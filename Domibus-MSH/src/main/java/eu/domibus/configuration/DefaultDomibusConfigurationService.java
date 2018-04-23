package eu.domibus.configuration;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DefaultDomibusConfigurationService implements DomibusConfigurationService {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public String getConfigLocation() {
        return System.getProperty(DOMIBUS_CONFIG_LOCATION);
    }

    //TODO add caching
    @Override
    public boolean isMultiTenantAware() {
        return StringUtils.isNotEmpty(domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY));
    }
}
