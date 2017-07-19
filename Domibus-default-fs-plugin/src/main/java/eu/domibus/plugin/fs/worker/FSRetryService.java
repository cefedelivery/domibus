package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSRetryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSRetryService.class);

    @Resource(name = "fsPluginProperties")
    private FSPluginProperties fsPluginProperties;

    @Autowired
    private ApplicationContext appContext;

    /**
     * Triggering the re-send means that the message file from the FAILED directory will be copied directly under the
     * corresponding OUT directory and eventually it will be treated like a normal file.
     */
    public void resendFailedFSMessages() {
        LOG.debug("Resending failed file system messages...");
        LOG.debug("location: {}", fsPluginProperties.getLocation());
        LOG.debug("location DOMAIN1: {}", fsPluginProperties.getLocation("DOMAIN1"));
    }

}
