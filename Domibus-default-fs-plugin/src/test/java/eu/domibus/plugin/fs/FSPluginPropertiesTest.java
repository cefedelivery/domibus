package eu.domibus.plugin.fs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPluginPropertiesTest {
    
    private static final String DEFAULT_PROPERTIES_PATH = "./src/test/resources/fsPlugin.properties";
    
    private static final String DOMAIN1 = "DOMAIN1";
    private static final String NONEXISTENT_DOMAIN = "NONEXISTENT_DOMAIN";
    
    private static final String DEFAULT_LOCATION = "/tmp/fs_plugin_data";
    private static final String DOMAIN1_LOCATION = "/tmp/fs_plugin_data/DOMAIN1";

    private FSPluginProperties fSPluginProperties;

    @Before
    public void setUp() throws Exception {
        fSPluginProperties = new FSPluginProperties();
        Properties properties = new Properties();
        properties.load(new FileInputStream(DEFAULT_PROPERTIES_PATH));
        fSPluginProperties.setProperties(properties);
    }

    @Test
    public void getLocationTest() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation());
    }
    
    @Test
    public void getLocationInDomainTest() throws Exception {
        Assert.assertEquals(DOMAIN1_LOCATION, fSPluginProperties.getLocation(DOMAIN1));
    }

    @Test
    public void getLocationInNonExistentDomainTest() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation(NONEXISTENT_DOMAIN));
    }

    @Test
    public void getSentActionTest() throws Exception {
        Assert.assertEquals(FSPluginProperties.ACTION_DELETE, fSPluginProperties.getSentAction());
    }

    @Test
    public void getSentPurgeWorkerCronExpressionTest() throws Exception {
        Assert.assertEquals("0/60 * * * * ?", fSPluginProperties.getSentPurgeWorkerCronExpression());
    }
    
    @Test
    public void getSentPurgeExpiredTest() throws Exception {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getSentPurgeExpired());
    }

    @Test
    public void getFailedActionTest() throws Exception {
        Assert.assertEquals(FSPluginProperties.ACTION_ARCHIVE, fSPluginProperties.getFailedAction());
    }

    @Test
    public void getFailedPurgeWorkerCronExpressionTest() throws Exception {
        Assert.assertEquals("0/60 * * * * ?", fSPluginProperties.getFailedPurgeWorkerCronExpression());
    }

    @Test
    public void getFailedPurgeExpiredTest() throws Exception {
        Assert.assertEquals(null, fSPluginProperties.getFailedPurgeExpired());
    }

    @Test
    public void getReceivedPurgeExpiredTest() throws Exception {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getReceivedPurgeExpired());
    }

    @Test
    public void getReceivedPurgeWorkerCronExpressionTest() throws Exception {
        Assert.assertEquals("0/60 * * * * ?", fSPluginProperties.getReceivedPurgeWorkerCronExpression());
    }

    @Test
    public void getUserTest() throws Exception {
        Assert.assertEquals("user1", fSPluginProperties.getUser(DOMAIN1));
    }

    @Test
    public void getPasswordTest() throws Exception {
        Assert.assertEquals("pass1", fSPluginProperties.getPassword(DOMAIN1));
    }

    @Test
    public void getExpressionTest() throws Exception {
        Assert.assertEquals("bdx:noprocess#TC1Leg1", fSPluginProperties.getExpression(DOMAIN1));
    }

    @Test
    public void getDomainsTest() throws Exception {
        Set<String> expected = new HashSet<>();
        expected.add(DOMAIN1);
        Assert.assertEquals(expected, fSPluginProperties.getDomains());
    }

}