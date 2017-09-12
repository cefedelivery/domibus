package eu.domibus.plugin.fs.worker;

import org.springframework.stereotype.Service;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSFilesManager;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSPurgeSentService extends FSAbstractPurgeService {
    
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeSentService.class);
    
    @Override
    public void purgeMessages() {
        LOG.debug("Purging sent file system messages...");

        super.purgeMessages();
    }

    @Override
    protected String getTargetFolderName() {
        return FSFilesManager.SENT_FOLDER;
    }

    @Override
    protected Integer getExpirationLimit(String domain) {
        return fsPluginProperties.getSentPurgeExpired(domain);
    }

}
