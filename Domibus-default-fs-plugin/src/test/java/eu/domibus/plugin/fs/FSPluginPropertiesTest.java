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
    
    private static final String DOMAIN1 = "DOMAIN1";
    private static final String DOMAIN2 = "DOMAIN2";
    private static final String NONEXISTENT_DOMAIN = "NONEXISTENT_DOMAIN";
    
    private static final String DEFAULT_LOCATION = "/tmp/fs_plugin_data";
    private static final String DOMAIN1_LOCATION = "/tmp/fs_plugin_data/DOMAIN1";

    private FSPluginProperties fSPluginProperties;

    @Before
    public void setUp() throws Exception {
        fSPluginProperties = new FSPluginProperties();
        Properties properties = new Properties();
        properties.load(FSTestHelper.getTestResource(this.getClass(), "fs-plugin.properties"));
        fSPluginProperties.setProperties(properties);
    }

    @Test
    public void testGetLocation() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation());
    }
    
    @Test
    public void testGetLocation_Domain1() throws Exception {
        Assert.assertEquals(DOMAIN1_LOCATION, fSPluginProperties.getLocation(DOMAIN1));
    }

    @Test
    public void testGetLocation_NonExistentDomain() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation(NONEXISTENT_DOMAIN));
    }

    @Test
    public void testGetSentAction() throws Exception {
        Assert.assertEquals(FSPluginProperties.ACTION_DELETE, fSPluginProperties.getSentAction());
    }

    @Test
    public void testGetSentPurgeWorkerCronExpression() throws Exception {
        Assert.assertEquals("0/60 * * * * ?", fSPluginProperties.getSentPurgeWorkerCronExpression());
    }
    
    @Test
    public void testGetSentPurgeExpired() throws Exception {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getSentPurgeExpired());
    }

    @Test
    public void testGetFailedAction() throws Exception {
        Assert.assertEquals(FSPluginProperties.ACTION_ARCHIVE, fSPluginProperties.getFailedAction());
    }

    @Test
    public void testGetFailedPurgeWorkerCronExpression() throws Exception {
        Assert.assertEquals("0/60 * * * * ?", fSPluginProperties.getFailedPurgeWorkerCronExpression());
    }

    @Test
    public void testGetFailedPurgeExpired() throws Exception {
        Assert.assertEquals(null, fSPluginProperties.getFailedPurgeExpired());
    }

    @Test
    public void testGetReceivedPurgeExpired() throws Exception {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getReceivedPurgeExpired());
    }

    @Test
    public void testGetReceivedPurgeWorkerCronExpression() throws Exception {
        Assert.assertEquals("0/60 * * * * ?", fSPluginProperties.getReceivedPurgeWorkerCronExpression());
    }

    @Test
    public void testGetUser() throws Exception {
        Assert.assertEquals("user1", fSPluginProperties.getUser(DOMAIN1));
    }

    @Test
    public void testGetPassword() throws Exception {
        Assert.assertEquals("pass1", fSPluginProperties.getPassword(DOMAIN1));
    }

    @Test
    public void testGetUser_NotSecured() throws Exception {
        Assert.assertEquals("", fSPluginProperties.getUser(DOMAIN2));
    }

    @Test
    public void testGetPassword_NotSecured() throws Exception {
        Assert.assertEquals("", fSPluginProperties.getPassword(DOMAIN2));
    }

    @Test
    public void testGetExpression_Domain1() throws Exception {
        Assert.assertEquals("bdx:noprocess#TC1Leg1", fSPluginProperties.getExpression(DOMAIN1));
    }

    @Test
    public void testGetExpression_Domain2() throws Exception {
        Assert.assertEquals("bdx:noprocess#TC1Leg2", fSPluginProperties.getExpression(DOMAIN2));
    }

    @Test
    public void testGetDomains() throws Exception {
        Set<String> expected = new HashSet<>();
        expected.add(DOMAIN1);
        expected.add(DOMAIN2);
        Assert.assertEquals(expected, fSPluginProperties.getDomains());
    }

}