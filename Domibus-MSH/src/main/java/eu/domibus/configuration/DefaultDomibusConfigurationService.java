package eu.domibus.configuration;

import eu.domibus.api.configuration.DomibusConfigurationService;
import org.springframework.stereotype.Component;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DefaultDomibusConfigurationService implements DomibusConfigurationService {

    @Override
    public String getConfigLocation() {
        return System.getProperty(DOMIBUS_CONFIG_LOCATION);
    }
}
