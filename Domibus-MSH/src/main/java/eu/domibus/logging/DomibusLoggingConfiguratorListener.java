package eu.domibus.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Christian Koch, Stefan Mueller, Cosmin Baciu
 */
public class DomibusLoggingConfiguratorListener implements ServletContextListener {

    private static final Log LOG = LogFactory.getLog(DomibusLoggingConfiguratorListener.class);

    private static final String LOG4J_FILE_NAME_PARAM = "log4jFileName";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String configuredLog4jFilename = servletContext.getInitParameter(LOG4J_FILE_NAME_PARAM);

        try {
            DomibusLoggingConfigurator domibusLoggingConfigurator = new DomibusLoggingConfigurator();
            domibusLoggingConfigurator.configureLogging(configuredLog4jFilename);
        } catch (Exception e) {
            //logging configuration problems should not prevent the application to startup
            LOG.warn("Error occured while configuring logging", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
