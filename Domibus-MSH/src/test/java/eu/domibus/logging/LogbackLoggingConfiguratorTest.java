package eu.domibus.logging;

import eu.domibus.api.configuration.DomibusConfigurationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Cosmin Baciu on 12-Oct-16.
 */
@RunWith(JMockit.class)
public class LogbackLoggingConfiguratorTest {

    @Mocked
    LoggerFactory LOG;

    @Tested
    LogbackLoggingConfigurator logbackLoggingConfigurator;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Test
    public void testConfigureLoggingWithCustomFile(@Mocked System mock) throws Exception {
        new Expectations(logbackLoggingConfigurator) {{
            logbackLoggingConfigurator.getDefaultLogbackConfigurationFile();
            result = "/user/logback.xml";

            System.getProperty(anyString);
            result = "/user/mylogback.xml";
        }};

        logbackLoggingConfigurator.configureLogging();

        new Verifications() {{
            String fileLocation = null;
            logbackLoggingConfigurator.configureLogging(fileLocation = withCapture());
            times = 1;

            Assert.assertEquals("/user/mylogback.xml", fileLocation);
        }};
    }

    @Test
    public void testConfigureLoggingWithTheDefaultLogbackConfigurationFile(@Mocked System mock) throws Exception {
        new Expectations(logbackLoggingConfigurator) {{
            logbackLoggingConfigurator.getDefaultLogbackConfigurationFile();
            result = "/user/logback.xml";

            System.getProperty(anyString);
            result = null;
        }};

        logbackLoggingConfigurator.configureLogging();

        new Verifications() {{
            String fileLocation = null;
            logbackLoggingConfigurator.configureLogging(fileLocation = withCapture());
            times = 1;

            Assert.assertEquals("/user/logback.xml", fileLocation);
        }};
    }

    @Test
    public void testConfigureLoggingWithEmptyConfigLocation(final @Capturing Logger log) throws Exception {
        logbackLoggingConfigurator.configureLogging(null);

        new Verifications() {{
            log.warn(anyString);
            times = 1;
        }};
    }

    @Test
    public void testConfigureLoggingWithMissingLogFile(@Mocked File file) throws Exception {
        new Expectations(logbackLoggingConfigurator) {{
            new File(anyString).exists();
            result = false;
        }};

        logbackLoggingConfigurator.configureLogging("/user/logback.xml");

        new Verifications() {{
            logbackLoggingConfigurator.configureLogback(anyString);
            times = 0;
        }};
    }

    @Test
    public void testConfigureLoggingWithExistingLogFile(@Mocked File file) throws Exception {
        new Expectations(logbackLoggingConfigurator) {{
            new File(anyString).exists();
            result = true;

            logbackLoggingConfigurator.configureLogback(anyString);
            result = null;
        }};

        logbackLoggingConfigurator.configureLogging("/user/logback.xml");

        new Verifications() {{
            logbackLoggingConfigurator.configureLogback(anyString);
            times = 1;
        }};
    }

    @Test
    public void testGetDefaultLogbackConfigurationFileWithConfiguredDomibusLocation(@Mocked System mock) throws Exception {
        new Expectations(logbackLoggingConfigurator) {{
            domibusConfigurationService.getConfigLocation();
            result = "/user/mylogback.xml";
        }};

        logbackLoggingConfigurator.getDefaultLogbackConfigurationFile();

        new Verifications() {{
            String fileLocation = null;
            logbackLoggingConfigurator.getLogFileLocation(fileLocation = withCapture(), anyString);
            times = 1;

            Assert.assertEquals("/user/mylogback.xml", fileLocation);
        }};
    }

    @Test
    public void testGetDefaultLogbackFilePathWithMissingDomibusLocation(@Mocked System mock, final @Capturing Logger log) throws Exception {
        new Expectations(logbackLoggingConfigurator) {{
            domibusConfigurationService.getConfigLocation();
            result = null;
        }};

        logbackLoggingConfigurator.getDefaultLogbackConfigurationFile();

        new Verifications() {{
            log.error(anyString);
            times = 1;
        }};
    }

}
