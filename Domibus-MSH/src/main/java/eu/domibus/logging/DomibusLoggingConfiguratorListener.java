package eu.domibus.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLoggingConfiguratorListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusLoggingConfiguratorListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            LogbackLoggingConfigurator logbackLoggingConfigurator = new LogbackLoggingConfigurator();
            logbackLoggingConfigurator.configureLogging();
        } catch (Exception e) {
            //logging configuration problems should not prevent the application to startup
            LOG.warn("Error occured while configuring logging", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //nothing to clean
    }
}
