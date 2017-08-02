package eu.domibus.plugin.fs;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.HashSet;
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
        fSPluginProperties.load(new FileInputStream(DEFAULT_PROPERTIES_PATH));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getLocation() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation());
    }
    
    @Test
    public void getLocationInDomain() throws Exception {
        Assert.assertEquals(DOMAIN1_LOCATION, fSPluginProperties.getLocation(DOMAIN1));
    }

    @Test
    public void getLocationInNonExistentDomain() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation(NONEXISTENT_DOMAIN));
    }

    @Test
    public void getSentAction() throws Exception {
        Assert.assertEquals("delete", fSPluginProperties.getSentAction());
    }

    @Test
    public void getSentPurgeWorkerCronExpression() throws Exception {
        Assert.assertEquals("0/60 * * * * ?", fSPluginProperties.getSentPurgeWorkerCronExpression());
    }
    
    @Test
    public void getSentPurgeExpired() throws Exception {
        Assert.assertEquals(600, fSPluginProperties.getSentPurgeExpired());
    }

    @Test
    public void getUser() throws Exception {
        Assert.assertEquals("user1", fSPluginProperties.getUser(DOMAIN1));
    }

    @Test
    public void getPassword() throws Exception {
        Assert.assertEquals("pass1", fSPluginProperties.getPassword(DOMAIN1));
    }

    @Test
    public void getDomains() throws Exception {
        Set<String> expected = new HashSet<>();
        expected.add(DOMAIN1);
        Assert.assertEquals(expected, fSPluginProperties.getDomains());
    }

}