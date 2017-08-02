package eu.domibus.plugin.fs;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPluginPropertiesTest {
    
    private static final String DEFAULT_PROPERTIES_PATH = "./src/test/resources/fsPlugin.properties";
    
    private static final String DOMAIN = "DOMAIN1";
    
    private static final String LOCATION = "/tmp/fs_plugin_data";
    private static final String DOMAIN_LOCATION = "/tmp/fs_plugin_data/DOMAIN1";
    private static final String SENT_ACTION = "delete";
    private static final String SENT_PURGE_WORKER_CRON_EXP = "0/60 * * * * ?";
    private static final int SENT_PURGE_EXPIRED = 600;
    
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
        Assert.assertEquals(LOCATION, fSPluginProperties.getLocation());
    }
    
    @Test
    public void getLocationInDomain() throws Exception {
        Assert.assertEquals(DOMAIN_LOCATION, fSPluginProperties.getLocation(DOMAIN));
    }

    @Test
    public void getSentAction() throws Exception {
        Assert.assertEquals(SENT_ACTION, fSPluginProperties.getSentAction());
    }

    @Test
    public void getSentPurgeWorkerCronexpression() throws Exception {
        Assert.assertEquals(SENT_PURGE_WORKER_CRON_EXP, fSPluginProperties.getSentPurgeWorkerCronExpression());
    }
    
    @Test
    public void getSentPurgeExpired() throws Exception {
        Assert.assertEquals(SENT_PURGE_EXPIRED, fSPluginProperties.getSentPurgeExpired());
    }

}