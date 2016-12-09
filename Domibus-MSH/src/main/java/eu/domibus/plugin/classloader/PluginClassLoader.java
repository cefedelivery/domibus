package eu.domibus.plugin.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginClassLoader.class);

    public PluginClassLoader(File file, ClassLoader parent) throws MalformedURLException {
        super(discoverPlugins(file), parent);
    }

    protected static URL[] discoverPlugins(File file) throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        File[] pluginJarFiles = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (pluginJarFiles != null) {
            for (File pluginJar : pluginJarFiles) {
                urls.add(pluginJar.toURI().toURL());
            }
        }
        LOG.info("Adding the following plugins to the classpath: " + urls);
        return urls.toArray(new URL[urls.size()]);
    }
}
