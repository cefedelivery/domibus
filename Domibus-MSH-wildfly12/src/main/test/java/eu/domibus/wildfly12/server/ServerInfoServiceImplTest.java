package eu.domibus.wildfly12.server;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @since 4.0.1
 */
@RunWith(JMockit.class)
public class ServerInfoServiceImplTest {

    @Tested
    ServerInfoServiceImpl serverInfoService;

    @Test
    public void testGetUniqueServerName() {
        final String serverUniqueName = serverInfoService.getUniqueServerName();
        Assert.assertNotNull(serverUniqueName);
        Assert.assertTrue(serverUniqueName.contains("@"));
    }

    @Test
    public void testGetServerName() {

        final String serverName = serverInfoService.getServerName();
        Assert.assertNotNull(serverName);
        Assert.assertFalse(serverName.contains("@"));
    }

}