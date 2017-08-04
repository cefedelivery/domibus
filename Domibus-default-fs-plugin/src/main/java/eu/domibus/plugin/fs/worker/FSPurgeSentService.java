package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSPurgeSentService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeSentService.class);

    @Autowired
    private FSPluginProperties fsPluginProperties;

    /**
     * Triggering the purge means that the message files from the SENT directory 
     * older than X seconds will be removed
     */
    public void resendFailedFSMessages() {
        LOG.debug("Purging sent file system messages...");
        LOG.debug("location: {}", fsPluginProperties.getLocation());
        LOG.debug("location DOMAIN1: {}", fsPluginProperties.getLocation("DOMAIN1"));
    }

}
