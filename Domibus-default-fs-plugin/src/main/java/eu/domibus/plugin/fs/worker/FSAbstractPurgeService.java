package eu.domibus.plugin.fs.worker;

import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.FSPluginProperties;
import eu.domibus.plugin.fs.exception.FSSetUpException;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public abstract class FSAbstractPurgeService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSAbstractPurgeService.class);

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected FSFilesManager fsFilesManager;

    /**
     * Triggering the purge means that the message files from the target directory
     * older than X seconds will be removed
     */
    public void purgeMessages() {
        purgeMessages(null);

        for (String domain : fsPluginProperties.getDomains()) {
            purgeMessages(domain);
        }
    }

    private void purgeMessages(String domain) {
        FileObject[] contentFiles = null;
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject targetFolder = fsFilesManager.getEnsureChildFolder(rootDir, getTargetFolderName())) {

            contentFiles = fsFilesManager.findAllDescendantFiles(targetFolder);
            LOG.debug(Arrays.toString(contentFiles));
            
            Integer expirationLimit = getExpirationLimit(domain);

            for (FileObject processableFile : contentFiles) {
                checkAndPurge(processableFile, expirationLimit);
            }

        } catch (FileSystemException ex) {
            LOG.error("Error purging messages", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        } finally {
            if (contentFiles != null) {
                fsFilesManager.closeAll(contentFiles);
            }
        }
    }

    protected abstract String getTargetFolderName();

    private void checkAndPurge(FileObject file, Integer expirationLimit) {
        try {
            if (isFileOlder(file, expirationLimit)) {
                LOG.debug("File [{}] is too old. Deleting", file.getName());
                fsFilesManager.deleteFile(file);
            } else {
                LOG.debug("File [{}] is young enough. Keeping it", file.getName());
            }
        } catch (FileSystemException ex) {
            LOG.error("Error processing file " + file.getName().getURI(), ex);
        }
    }

    protected abstract Integer getExpirationLimit(String domain);

    private boolean isFileOlder(FileObject file, Integer expirationLimit) throws FileSystemException {
        long currentMillis = System.currentTimeMillis();
        long modifiedMillis = file.getContent().getLastModifiedTime();
        long fileAgeSeconds = (currentMillis - modifiedMillis) / 1000;

        return fileAgeSeconds > expirationLimit;
    }

}
