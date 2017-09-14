package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.exception.FSSetUpException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSSendMessagesService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSSendMessagesService.class);
    
    public static final String METADATA_FILE_NAME = "metadata.xml";

    @Autowired
    private FSPluginProperties fsPluginProperties;
    
    @Autowired
    private FSFilesManager fsFilesManager;
    
    @Autowired
    private FSProcessFileService fsProcessFileService;
    
    /**
     * Triggering the send messages means that the message files from the OUT directory
     * will be processed to be sent
     */
    public void sendMessages() {
        LOG.debug("Sending file system messages...");
        
        sendMessages(null);
        
        for (String domain : fsPluginProperties.getDomains()) {
            sendMessages(domain);
        }
    }
    
    private void sendMessages(String domain) {
        FileObject[] contentFiles = null;
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER)) {
            
            contentFiles = fsFilesManager.findAllDescendantFiles(outgoingFolder);
            LOG.debug(Arrays.toString(contentFiles));

            List<FileObject> processableFiles = filterProcessableFiles(contentFiles);
            for (FileObject processableFile : processableFiles) {
                processFileSafely(processableFile);
            }

        } catch (FileSystemException ex) {
            LOG.error("Error sending messages", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        } finally {
            if (contentFiles != null) {
                fsFilesManager.closeAll(contentFiles);
            }
        }
    }

    private void processFileSafely(FileObject processableFile) {
        try {
            fsProcessFileService.processFile(processableFile);
        } catch (RuntimeException ex) {
            LOG.error("Error processing file [" + processableFile.getName().getURI() + "]. Skipped it", ex);
        }
    }

    private List<FileObject> filterProcessableFiles(FileObject[] files) {
        List<FileObject> filteredFiles = new LinkedList<>();
        
        for (FileObject file : files) {
            String baseName = file.getName().getBaseName();
            
            if (!StringUtils.equals(baseName, METADATA_FILE_NAME)
                    && !FSFileNameHelper.isAnyState(baseName)
                    && !FSFileNameHelper.isProcessed(baseName)) {
                filteredFiles.add(file);
            }
        }
        
        return filteredFiles;
    }

}
