package eu.domibus.spring;

import eu.domibus.plugin.classloader.PluginClassLoader;
import eu.domibus.property.PropertyResolverBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by Cosmin Baciu on 6/13/2016.
 */
public class DomibusContextLoaderListener extends ContextLoaderListener {

    private static final Log LOG = LogFactory.getLog(DomibusContextLoaderListener.class);

    PluginClassLoader pluginClassLoader = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String pluginsLocation = servletContext.getInitParameter("pluginsLocation");
        if (StringUtils.isEmpty(pluginsLocation)) {
            throw new RuntimeException("pluginsLocation context param should not be empty");
        }
        String resolvedPluginsLocation = PropertyResolverBuilder.create().build().getResolvedProperty(pluginsLocation);
        LOG.info("Resolved plugins location [" + pluginsLocation + "] to [" + resolvedPluginsLocation + "]");

        try {
            pluginClassLoader = new PluginClassLoader(new File(resolvedPluginsLocation), Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Thread.currentThread().setContextClassLoader(pluginClassLoader);
        super.contextInitialized(servletContextEvent);
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
