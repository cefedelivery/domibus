package eu.domibus.common.util;

import mockit.integration.junit4.JMockit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Federico Martini
 */
@RunWith(JMockit.class)
public class ManifestServiceTest {

    private static final Log LOG = LogFactory.getLog(ManifestServiceTest.class);

    private String path = System.getProperty("user.dir") + "/src/test/resources/";

    @Test
    public void testDisplayVersion() throws Exception {

        ManifestService manService = new ManifestService(path);

        assertEquals("domibus-MSH", manService.getSpecTitle());
        assertNotEquals("", manService.getBuiltTime());
        assertNotEquals("", manService.getImplVersion());
        assertNotEquals("", manService.getBuiltBy());

        LOG.info(manService.getDisplayVersion());
        LOG.info(manService.getBuiltBy());
    }
}
