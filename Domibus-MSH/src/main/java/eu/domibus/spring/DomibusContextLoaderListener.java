package eu.domibus.spring;

import eu.domibus.plugin.classloader.PluginClassLoader;
import eu.domibus.property.PropertyResolverBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Created by Cosmin Baciu on 6/13/2016.
 */
public class DomibusContextLoaderListener extends ContextLoaderListener {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusContextLoaderListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String pluginsLocation = servletContext.getInitParameter("pluginsLocation");
        if (StringUtils.isEmpty(pluginsLocation)) {
            throw new RuntimeException("pluginsLocation context param should not be empty");
        }
        String resolvedPluginsLocation = PropertyResolverBuilder.create().build().getResolvedProperty(pluginsLocation);
        LOG.info("Resolved plugins location [" + pluginsLocation + "] to [" + resolvedPluginsLocation + "]");

        ClassLoader pluginClassLoader = null;
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
    }

}
