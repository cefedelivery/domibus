/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.common.util;

import eu.domibus.common.exception.ConfigurationException;
import org.springframework.web.util.Log4jWebConfigurer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class DomibusLocationLog4jConfigureListener implements ServletContextListener {
    private static final String LOG4J_FILE_NAME_PARAM = "log4jFileName";
    private static final String FILE_PREFIX = "file:";

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();


        final String domibusConfigLocation = System.getProperty("domibus.config.location");

        final String configuredLog4jFilename = servletContext.getInitParameter(LOG4J_FILE_NAME_PARAM);

        if (domibusConfigLocation == null || configuredLog4jFilename == null || "".equals(configuredLog4jFilename)) {
            throw new ConfigurationException("Please check you configuration. domibus.config.location or/and log4jFileName (in web.xml) is/are not configured.");
        }

        servletContext.setInitParameter(Log4jWebConfigurer.CONFIG_LOCATION_PARAM, FILE_PREFIX + domibusConfigLocation + File.separator + configuredLog4jFilename);

        Log4jWebConfigurer.initLogging(servletContext);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        Log4jWebConfigurer.shutdownLogging(sce.getServletContext());
    }
}
