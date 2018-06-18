package eu.domibus.ext.delegate.services.multitenant;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class DomibusConfigurationServiceDelegate implements DomibusConfigurationExtService {

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Override
    public boolean isMultiTenantAware() {
        return domibusConfigurationService.isMultiTenantAware();
    }
}
