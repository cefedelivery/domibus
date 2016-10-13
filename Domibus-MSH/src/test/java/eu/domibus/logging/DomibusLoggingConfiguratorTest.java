package eu.domibus.logging;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.logging.Log;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Created by Cosmin Baciu on 12-Oct-16.
 */
@RunWith(JMockit.class)
public class DomibusLoggingConfiguratorTest {

    @Mocked
    PropertyConfigurator mock;

    @Tested
    DomibusLoggingConfigurator domibusLoggingConfigurator;

    @Test
    public void testConfigureLoggingWithCustomLog4jFile(@Mocked System mock) throws Exception {
        new Expectations(domibusLoggingConfigurator) {{
            domibusLoggingConfigurator.getDefaultLog4jFilePath();
            result = "/user/log4j.properties";

            System.getProperty(anyString);
            result = "/user/mylog4j.properties";
        }};

        domibusLoggingConfigurator.configureLogging();

        new Verifications() {{
            String fileLocation = null;
            domibusLoggingConfigurator.configureLogging(fileLocation = withCapture());
            times = 1;

            Assert.assertEquals("/user/mylog4j.properties", fileLocation);
        }};
    }

    @Test
    public void testConfigureLoggingWithTheDefaultLog4jFile(@Mocked System mock) throws Exception {
        new Expectations(domibusLoggingConfigurator) {{
            domibusLoggingConfigurator.getDefaultLog4jFilePath();
            result = "/user/log4j.properties";

            System.getProperty(anyString);
            result = null;
        }};

        domibusLoggingConfigurator.configureLogging();

        new Verifications() {{
            String fileLocation = null;
            domibusLoggingConfigurator.configureLogging(fileLocation = withCapture());
            times = 1;

            Assert.assertEquals("/user/log4j.properties", fileLocation);
        }};
    }

    @Test
    public void testConfigureLoggingWithEmptyConfigLocation(final @Capturing Log log) throws Exception {
        domibusLoggingConfigurator.configureLogging(null);

        new Verifications() {{
            PropertyConfigurator.configure(anyString);
            times = 0;

            log.warn(anyString);
            times = 1;
        }};
    }

    @Test
    public void testConfigureLoggingWithMissingLogFile(@Mocked File file) throws Exception {
        new Expectations() {{
            new File(anyString).exists();
            result = false;
        }};

        domibusLoggingConfigurator.configureLogging("/user/log4j.properties");

        new Verifications() {{
            PropertyConfigurator.configure(anyString);
            times = 0;
        }};
    }

    @Test
    public void testConfigureLoggingWithExistingLogFile(@Mocked File file) throws Exception {
        new Expectations() {{
            new File(anyString).exists();
            result = true;
        }};

        domibusLoggingConfigurator.configureLogging("/user/log4j.properties");

        new Verifications() {{
            PropertyConfigurator.configure(anyString);
            times = 1;
        }};
    }

    @Test
    public void testGetDefaultLog4jFilePathWithConfiguredDomibusLocation(@Mocked System mock) throws Exception {
        new Expectations(domibusLoggingConfigurator) {{
            System.getProperty(anyString);
            result = "/user/mylog4j.properties";
        }};

        domibusLoggingConfigurator.getDefaultLog4jFilePath();

        new Verifications() {{
            String fileLocation = null;
            domibusLoggingConfigurator.getLogFileLocation(fileLocation = withCapture(), anyString);
            times = 1;

            Assert.assertEquals("/user/mylog4j.properties", fileLocation);
        }};
    }

    @Test
    public void testGetDefaultLog4jFilePathWithMissingDomibusLocation(@Mocked System mock, final @Capturing Log log) throws Exception {
        new Expectations(domibusLoggingConfigurator) {{
            System.getProperty(anyString);
            result = null;
        }};

        domibusLoggingConfigurator.getDefaultLog4jFilePath();

        new Verifications() {{
            log.error(anyString);
            times = 1;
        }};
    }

}
