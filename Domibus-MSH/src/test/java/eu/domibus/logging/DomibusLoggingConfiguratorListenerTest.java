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
    public void testContextInitialized() throws Exception {
        new Expectations() {{
            domibusLoggingConfigurator.configureLogging();
            result = null;
        }};

        domibusLoggingConfiguratorListener.contextInitialized(servletContextEvent);

        new Verifications() {{
            domibusLoggingConfigurator.configureLogging();
            times = 1;
        }};
    }

    @Test
    public void testContextInitializedWithException(final @Capturing Log log) throws Exception {
        new Expectations() {{
            domibusLoggingConfigurator.configureLogging();
            result = new RuntimeException("Error configuring the logging");
        }};

        domibusLoggingConfiguratorListener.contextInitialized(servletContextEvent);

        new Verifications() {{
            domibusLoggingConfigurator.configureLogging();
            times = 1;

            log.warn(anyString, withAny(new RuntimeException()));
            times = 1;
        }};
    }

    @Test
    public void testContextDestroyed() throws Exception {
        domibusLoggingConfiguratorListener.contextDestroyed(servletContextEvent);
    }
}
