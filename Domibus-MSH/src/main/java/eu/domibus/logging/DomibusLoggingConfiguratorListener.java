package eu.domibus.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLoggingConfiguratorListener implements ServletContextListener {

    private static final Log LOG = LogFactory.getLog(DomibusLoggingConfiguratorListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            DomibusLoggingConfigurator domibusLoggingConfigurator = new DomibusLoggingConfigurator();
            domibusLoggingConfigurator.configureLogging();
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
