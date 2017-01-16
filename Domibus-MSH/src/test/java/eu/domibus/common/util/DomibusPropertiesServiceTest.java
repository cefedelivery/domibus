package eu.domibus.common.util;

import mockit.integration.junit4.JMockit;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Federico Martini
 */
@RunWith(JMockit.class)
public class DomibusPropertiesServiceTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertiesServiceTest.class);

    @Test
    public void testDisplayVersion() throws Exception {

        DomibusPropertiesService service = new DomibusPropertiesService();

        assertEquals("domibus-MSH", service.getArtifactName());
        assertNotEquals("", service.getBuiltTime());
        assertNotEquals("", service.getImplVersion());

        LOG.info(service.getDisplayVersion());
    }
}
