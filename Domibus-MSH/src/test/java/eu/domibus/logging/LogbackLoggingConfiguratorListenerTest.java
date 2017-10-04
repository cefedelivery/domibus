package eu.domibus.logging;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.slf4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Created by Cosmin Baciu on 12-Oct-16.
 */
@RunWith(JMockit.class)
public class LogbackLoggingConfiguratorListenerTest {

    @Injectable
    ServletContextEvent servletContextEvent;

    @Injectable
    ServletContext servletContext;

    @Tested
    DomibusLoggingConfiguratorListener domibusLoggingConfiguratorListener;

    @Mocked
    LogbackLoggingConfigurator logbackLoggingConfigurator;

    @Test
    public void testContextInitialized() throws Exception {
        new Expectations() {{
            logbackLoggingConfigurator.configureLogging();
            result = null;
        }};

        domibusLoggingConfiguratorListener.contextInitialized(servletContextEvent);

        new Verifications() {{
            logbackLoggingConfigurator.configureLogging();
            times = 1;
        }};
    }

    @Test
    public void testContextInitializedWithException(final @Capturing Logger log) throws Exception {
        new Expectations() {{
            logbackLoggingConfigurator.configureLogging();
            result = new RuntimeException("Error configuring the logging");
        }};

        domibusLoggingConfiguratorListener.contextInitialized(servletContextEvent);

        new Verifications() {{
            logbackLoggingConfigurator.configureLogging();
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
