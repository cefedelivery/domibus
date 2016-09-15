package eu.domibus.property;

import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
@RunWith(JMockit.class)
public class PropertyResolverBuilderTest {

    @Test
    public void testResolveProperty() throws Exception {
        PropertyResolver propertyResolver = PropertyResolverBuilder
                .create()
                .startDelimiter("[")
                .endDelimiter("]")
                .resolveLevel(5)
                .build();
        assertEquals(propertyResolver.getStartDelimiter(), "[");
        assertEquals(propertyResolver.getEndDelimiter(), "]");
        assertEquals(propertyResolver.getResolveLevel(), Integer.valueOf(5));
    }

}
