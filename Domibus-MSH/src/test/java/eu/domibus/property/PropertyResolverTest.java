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

        String resolvedProperty = propertyResolver.getResolvedProperty("wsPluginLocation", properties, false);
        assertEquals(resolvedProperty, "c:/plugins/ws");
    }

    @Test
    public void testResolvePropertyWithSystemVariables() throws Exception {
        Properties properties = new Properties();
        System.setProperty("domibus.conf.location", "c:");
        properties.setProperty("pluginsDirectory", "${domibus.conf.location}/plugins");
        properties.setProperty("wsPluginLocation", "${pluginsDirectory}/ws");

        String resolvedProperty = propertyResolver.getResolvedProperty("wsPluginLocation", properties, true);
        assertEquals(resolvedProperty, "c:/plugins/ws");
    }

    @Test
    public void testResolvePropertyWithOnlySystemVariables() throws Exception {
        System.setProperty("domibus.conf.location", "c:");

        String resolvedProperty = propertyResolver.getResolvedProperty("domibus.conf.location");
        assertEquals(resolvedProperty, "c:");
    }

    @Test
    public void testResolvePropertyWithNullValueForVariable() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("wsPluginLocation", "${pluginsDirectory}/ws");

        String resolvedProperty = propertyResolver.getResolvedProperty("wsPluginLocation", properties, false);
        assertEquals(resolvedProperty, "${pluginsDirectory}/ws");
    }

    @Test
    public void testResolveValue() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("wsPluginLocation", "${pluginsDirectory}/plugins");
        properties.setProperty("pluginsDirectory", "/home");

        String resolvedProperty = propertyResolver.getResolvedValue("${wsPluginLocation}/ws", properties, false);
        assertEquals(resolvedProperty, "/home/plugins/ws");
    }

    @Test
    public void testResolveValueWithInvalidProperty() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("wsPluginLocation", "${pluginsDirectory}/plugins");

        String resolvedProperty = propertyResolver.getResolvedValue("${wsPluginLocation/ws", properties, false);
        assertEquals(resolvedProperty, "${wsPluginLocation/ws");
    }

    @Test
    public void testResolvePropertyWithNullVariable() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("wsPluginLocation", "${pluginsDirectory}/ws");

        String resolvedProperty = propertyResolver.getResolvedProperty(null, properties, false);
        assertEquals(resolvedProperty, null);
    }
}
