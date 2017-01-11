package eu.domibus.logging;

import eu.domibus.configuration.DefaultDomibusConfigurationService;
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
            //at this stage Spring is not yet initialized so we need to perform manually the injection of the configuration service
            logbackLoggingConfigurator.setDomibusConfigurationService(new DefaultDomibusConfigurationService());
            logbackLoggingConfigurator.configureLogging();
        } catch (Exception e) {
            //logging configuration problems should not prevent the application to startup
            LOG.warn("Error occurred while configuring logging", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //nothing to clean
    }
}
