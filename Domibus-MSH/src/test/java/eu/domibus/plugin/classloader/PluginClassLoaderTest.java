package eu.domibus.plugin.classloader;

import mockit.Expectations;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
@RunWith(JMockit.class)
public class PluginClassLoaderTest {

    @Injectable
    File pluginsDir;

    @Test
    public void testDiscoverPlugins() throws Exception {
        final File plugin1JarFile = new File("c:/plugin1.jar");
        final File plugin2JarFile = new File("c:/plugin2.jar");
        new Expectations() {{
            pluginsDir.listFiles(withAny(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return false;
                }
            }));
            result = new File[]{plugin1JarFile, plugin2JarFile};
        }};


        PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginsDir, PluginClassLoaderTest.class.getClassLoader());
        URL[] urls = pluginClassLoader.getURLs();
        assertNotNull(urls);
        assertEquals(urls.length, 2);

        List<URL> discoveredPluginsURL = Arrays.asList(urls);
        discoveredPluginsURL.contains(plugin1JarFile.toURI().toURL());
        discoveredPluginsURL.contains(plugin2JarFile.toURI().toURL());
    }
}
