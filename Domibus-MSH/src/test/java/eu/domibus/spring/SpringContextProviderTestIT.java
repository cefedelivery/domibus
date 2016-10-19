package eu.domibus.spring;

import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * @author  Cosmin Baciu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class SpringContextProviderTestIT {

    @Configuration
    static class ContextConfiguration {
    }

    @Test
    public void testGetApplicationContext() throws Exception {
        ApplicationContext applicationContext = SpringContextProvider.getApplicationContext();
        Assert.assertNull(applicationContext);

    }
}
