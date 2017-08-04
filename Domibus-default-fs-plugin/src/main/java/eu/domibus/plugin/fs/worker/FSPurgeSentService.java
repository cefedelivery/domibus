package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSPluginProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSPurgeSentService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeSentService.class);

    @Resource(name = "fsPluginProperties")
    private FSPluginProperties fsPluginProperties;

    /**
     * Triggering the purge means that the message files from the SENT directory 
     * older than X seconds will be removed
     */
    public void resendFailedFSMessages() {
        LOG.debug("Purging sent file system messages...");
    }

}
