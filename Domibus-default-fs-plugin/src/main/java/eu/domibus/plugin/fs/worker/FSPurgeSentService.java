package eu.domibus.plugin.fs.worker;

import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSPluginProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.exception.FSSetUpException;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSPurgeSentService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeSentService.class);

    @Autowired
    private FSPluginProperties fsPluginProperties;
    
    @Autowired
    private FSFilesManager fsFilesManager;

    /**
     * Triggering the purge means that the message files from the SENT directory 
     * older than X seconds will be removed
     */
    public void purgeSentFSMessages() {
        LOG.debug("Purging sent file system messages...");
        
        purgeMessages(null);
        
        for (String domain : fsPluginProperties.getDomains()) {
            purgeMessages(domain);
        }
    }
    
    private void purgeMessages(String domain) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject sentFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.SENT_FOLDER)) {
            
            FileObject[] contentFiles = fsFilesManager.findAllDescendantFiles(sentFolder);
            LOG.debug(Arrays.toString(contentFiles));

            for (FileObject processableFile : contentFiles) {
                checkAndPurge(domain, processableFile);
            }

            fsFilesManager.closeAll(contentFiles);
        } catch (FileSystemException ex) {
            LOG.error("Error purging sent messages", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        }
    }
    
    private void checkAndPurge(String domain, FileObject file) {
        try {
            Integer expirationInterval = fsPluginProperties.getSentPurgeExpired(domain);
            if (isFileOlder(file, expirationInterval)) {
                LOG.debug("File {} is too old. Deleting", file.getName());
                fsFilesManager.deleteFile(file);
            } else {
                LOG.debug("File {} is young enough. Keeping it", file.getName());
            }
        } catch (FileSystemException ex) {
            LOG.error("Error processing file " + file.getName().getURI(), ex);
        }
    }

    private static boolean isFileOlder(FileObject file, Integer expirationInterval) throws FileSystemException {
        long currentMillis = System.currentTimeMillis();
        long modifiedMillis = file.getContent().getLastModifiedTime();
        long fileAgeSeconds = (currentMillis - modifiedMillis)/1000;
        
        return fileAgeSeconds > expirationInterval;
    }

}
