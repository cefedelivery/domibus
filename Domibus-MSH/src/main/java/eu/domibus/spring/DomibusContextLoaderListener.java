package eu.domibus.spring;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.plugin.PluginException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.classloader.PluginClassLoader;
import eu.domibus.property.PropertyResolver;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Security;

/**
 * Created by Cosmin Baciu on 6/13/2016.
 */
public class DomibusContextLoaderListener extends ContextLoaderListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextLoaderListener.class);

    PluginClassLoader pluginClassLoader = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        registerBouncyCastle();

        ServletContext servletContext = servletContextEvent.getServletContext();
        String pluginsLocation = servletContext.getInitParameter("pluginsLocation");
        if (StringUtils.isEmpty(pluginsLocation)) {
            throw new PluginException(DomibusCoreErrorCode.DOM_001, "pluginsLocation context param should not be empty");
        }
        String resolvedPluginsLocation = new PropertyResolver().getResolvedValue(pluginsLocation);
        LOG.info("Resolved plugins location [" + pluginsLocation + "] to [" + resolvedPluginsLocation + "]");

        try {
            pluginClassLoader = new PluginClassLoader(new File(resolvedPluginsLocation), Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new PluginException(DomibusCoreErrorCode.DOM_001, "Malformed URL Exception", e);
        }
        Thread.currentThread().setContextClassLoader(pluginClassLoader);
        super.contextInitialized(servletContextEvent);
    }

    protected void registerBouncyCastle() {
        LOG.info("Registering BouncyCastle...");
        final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.insertProviderAt (bouncyCastleProvider, 1);
        LOG.info("Registered BouncyCastle.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        if (pluginClassLoader != null) {
            try {
                pluginClassLoader.close();
            } catch (IOException e) {
                LOG.warn("Error closing PluginClassLoader", e);
            }
        }
    }
}
