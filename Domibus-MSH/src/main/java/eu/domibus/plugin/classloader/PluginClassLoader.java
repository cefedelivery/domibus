package eu.domibus.plugin.classloader;

import com.google.common.collect.Lists;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
public class PluginClassLoader extends URLClassLoader {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginClassLoader.class);

    public PluginClassLoader(Set<File> files, ClassLoader parent) throws MalformedURLException {
        super(discoverPlugins(files), parent);
    }

    /**
     * Group the plugins and extension directories to extract the jar files url.
     * @param directories set of extension/plugins directories.
     * @return the urls of the jar files.
     * @throws MalformedURLException
     */
    protected static URL[] discoverPlugins(Set<File> directories) throws MalformedURLException {

        final List<URI> jarUris = directories.stream().
                map(directory -> {
                    LOG.debug("Extracting plugin and extension jar files from directory:[{}]",directory);
                    return directory.listFiles((dir, name) -> name.endsWith(".jar"));
                }).
                filter(Objects::nonNull).
                map(Lists::newArrayList).
                flatMap(ArrayList::stream).
                map(File::toURI).
                collect(Collectors.toList());

        final URL[] urls = new URL[jarUris.size()];
        for (int i = 0; i < jarUris.size(); i++) {
            urls[i] = jarUris.get(i).toURL();
            LOG.info("Adding the following plugin/extension to the classpath:[{}] ", urls[i]);
        }
        return urls;
    }
}
