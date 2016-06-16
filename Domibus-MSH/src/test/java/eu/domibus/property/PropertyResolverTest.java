package eu.domibus.property;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
@RunWith(JMockit.class)
public class PropertyResolverTest {

    @Tested
    PropertyResolver propertyResolver;

    @Test
    public void testResolveProperty() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("domibus.conf.location", "c:");
        properties.setProperty("pluginsDirectory", "${domibus.conf.location}/plugins");
        properties.setProperty("wsPluginLocation", "${pluginsDirectory}/ws");

        String resolvedProperty = propertyResolver.getResolvedProperty("${wsPluginLocation}",properties, false);
        assertEquals(resolvedProperty, "c:/plugins/ws");
    }

    @Test
    public void testResolvePropertyWithSytemVariables() throws Exception {
        Properties properties = new Properties();
        System.setProperty("domibus.conf.location", "c:");
        properties.setProperty("pluginsDirectory", "${domibus.conf.location}/plugins");
        properties.setProperty("wsPluginLocation", "${pluginsDirectory}/ws");

        String resolvedProperty = propertyResolver.getResolvedProperty("${wsPluginLocation}",properties, true);
        assertEquals(resolvedProperty, "c:/plugins/ws");
    }
}
