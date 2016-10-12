package eu.domibus.logging;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
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
    public void testConfigureLoggingOnlyWithLogFileName(@Mocked System mock) throws Exception {
        new Expectations(domibusLoggingConfigurator) {{
            System.getProperty(anyString);
            result = "c:";
        }};

        domibusLoggingConfigurator.configureLogging("log4j.properties");

        new Verifications() {{
            String confLocation = null;
            String fileLocation = null;
            domibusLoggingConfigurator.configureLogging(confLocation = withCapture(), fileLocation = withCapture());
            times = 1;

            Assert.assertEquals("c:", confLocation);
            Assert.assertEquals("log4j.properties", fileLocation);
        }};
    }

    @Test
    public void testConfigureLoggingWithEmptyConfigLocation() throws Exception {
        domibusLoggingConfigurator.configureLogging("", "log4j.properties");

        new Verifications() {{
            PropertyConfigurator.configure(anyString);
            times = 0;
        }};
    }

    @Test
    public void testConfigureLoggingWithEmptyLogFileName(@Mocked File file) throws Exception {
        new Expectations() {{
            new File(anyString).exists();
            result = true;
        }};

        domibusLoggingConfigurator.configureLogging("c:", null);

        new Verifications() {{
            String logFileLocation = null;
            PropertyConfigurator.configure(logFileLocation = withCapture());
            times = 1;

            Assert.assertTrue(StringUtils.contains(logFileLocation, "log4j.properties"));
        }};
    }

    @Test
    public void testConfigureLoggingWithConfiguredFileName(@Mocked File file) throws Exception {
        new Expectations() {{
            new File(anyString).exists();
            result = true;
        }};

        domibusLoggingConfigurator.configureLogging("c:", "mylog4j.properties");

        new Verifications() {{
            String logFileLocation = null;
            PropertyConfigurator.configure(logFileLocation = withCapture());
            times = 1;

            Assert.assertTrue(StringUtils.contains(logFileLocation, "mylog4j.properties"));
        }};
    }

    @Test
    public void testConfigureLoggingWithMissingLogFile(@Mocked File file) throws Exception {
        new Expectations() {{
            new File(anyString).exists();
            result = false;
        }};

        domibusLoggingConfigurator.configureLogging("c:", "mylog4j.properties");

        new Verifications() {{
            PropertyConfigurator.configure(anyString);
            times = 0;
        }};
    }
}
