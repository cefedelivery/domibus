package eu.domibus.core.alerts.model.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.service.ConfigurationReader;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class ConfigurationLoaderTest {

    @Tested
    private ConfigurationLoader configurationLoader;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Test
    public void getConfigurationForDomain(@Mocked final ConfigurationReader configurationReader, @Mocked final Domain domain) {
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = domain;
        }};
        configurationLoader.getConfiguration(configurationReader);
        configurationLoader.getConfiguration(configurationReader);
        new Verifications() {{
            configurationReader.readConfiguration(domain);
            times = 1;
        }};
    }

    @Test
    public void getConfigurationForSuper(@Mocked final ConfigurationReader configurationReader) {
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = null;
        }};
        configurationLoader.getConfiguration(configurationReader);
        configurationLoader.getConfiguration(configurationReader);
        new Verifications() {{
            configurationReader.readConfiguration(ConfigurationLoader.SUPER_DOMAIN);
            times = 1;
        }};
    }
}