package eu.domibus.logging;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Created by Cosmin Baciu on 12-Oct-16.
 */
@RunWith(JMockit.class)
public class DomibusLoggingConfiguratorListenerTest {

    @Injectable
    ServletContextEvent servletContextEvent;

    @Injectable
    ServletContext servletContext;

    @Tested
    DomibusLoggingConfiguratorListener domibusLoggingConfiguratorListener;

    @Mocked
    DomibusLoggingConfigurator domibusLoggingConfigurator;

    @Test
    public void testConfigureLogging() throws Exception {
        new Expectations() {{
            servletContextEvent.getServletContext();
            result = servletContext;

            servletContext.getInitParameter("log4jFileName");
            result = "myLog4j.properties";

            domibusLoggingConfigurator.configureLogging("myLog4j.properties");
            result = null;
        }};

        domibusLoggingConfiguratorListener.contextInitialized(servletContextEvent);

        new Verifications() {{
            String fileLocation = null;
            domibusLoggingConfigurator.configureLogging(fileLocation = withCapture());
            times = 1;

            Assert.assertEquals("myLog4j.properties", fileLocation);
        }};
    }

    @Test
    public void testConfigureLoggingWithException(final @Capturing Log log) throws Exception {
        new Expectations() {{
            servletContextEvent.getServletContext();
            result = servletContext;

            servletContext.getInitParameter("log4jFileName");
            result = "myLog4j.properties";

            domibusLoggingConfigurator.configureLogging("myLog4j.properties");
            result = new RuntimeException("Error configuring the logging");
        }};

        domibusLoggingConfiguratorListener.contextInitialized(servletContextEvent);

        new Verifications() {{
            String fileLocation = null;
            domibusLoggingConfigurator.configureLogging(fileLocation = withCapture());
            times = 1;

            Assert.assertEquals("myLog4j.properties", fileLocation);

            log.warn(anyString, withAny(new RuntimeException()));
            times = 1;
        }};
    }


}
