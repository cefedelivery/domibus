package eu.domibus;

import eu.domibus.classloader.PluginClassLoader;
import eu.domibus.property.PropertyResolver;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Created by Cosmin Baciu on 6/13/2016.
 */
public class DomibusContextLoaderListener extends ContextLoaderListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String pluginsLocation = servletContext.getInitParameter("pluginsLocation");
        if (StringUtils.isEmpty(pluginsLocation)) {
            throw new RuntimeException("pluginsLocation context param should not be empty");
        }
        String resolvedPluginsLocation = PropertyResolver.create().getResolvedProperty(pluginsLocation);

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
