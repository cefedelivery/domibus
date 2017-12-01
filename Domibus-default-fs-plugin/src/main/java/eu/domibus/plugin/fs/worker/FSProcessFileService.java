package eu.domibus.plugin.fs.worker;

import eu.domibus.common.MSHRole;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSPluginException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSProcessFileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSProcessFileService.class);

    private static final String DEFAULT_CONTENT_ID = "cid:message";

    private static final String LS = System.lineSeparator();
    
    @Resource(name = "backendFSPlugin")
    private BackendFSImpl backendFSPlugin;

    @Autowired
    private FSPluginProperties fsPluginProperties;
    
    @Autowired
    private FSFilesManager fsFilesManager;

    @Autowired
    private BackendFSImpl backendFSImpl;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processFile(FileObject processableFile) {
        String domain = null;

        if(StringUtils.containsIgnoreCase(processableFile.getName().getBaseName(), "fmweb")) {
            LOG.info("Skipping temporary file [{}]", processableFile.getName().getBaseName());
            return;
        }

        try (FileObject metadataFile = fsFilesManager.resolveSibling(processableFile, FSSendMessagesService.METADATA_FILE_NAME)) {
            if (metadataFile.exists()) {
                UserMessage metadata = parseMetadata(metadataFile);
                LOG.debug("Metadata found and valid: [{}]", processableFile.getName());

                DataHandler dataHandler = fsFilesManager.getDataHandler(processableFile);
                Map<String, FSPayload> fsPayloads = new HashMap<>(1);
                final FSPayload value = new FSPayload(null, dataHandler);
                value.setFilename(processableFile.getName().getBaseName());
                fsPayloads.put(DEFAULT_CONTENT_ID, value);
                FSMessage message= new FSMessage(fsPayloads, metadata);
                domain = backendFSImpl.resolveDomain(message);
                String messageId = backendFSPlugin.submit(message);
                LOG.info("Message submitted: [{}]", processableFile.getName());

                renameProcessedFile(processableFile, messageId);
            } else {
                LOG.error("Metadata file is missing for " + processableFile.getName().getURI());
            }
        } catch (FileSystemException ex) {
            LOG.error("Error processing file " + processableFile.getName().getURI(), ex);
        } catch (JAXBException ex) {
            LOG.error("Metadata file is not an XML file", ex);
        } catch (Exception ex) {
            LOG.error("Error occurred submitting message to Domibus", ex);

            handleSendFailedMessage(processableFile, domain, null, ex.getMessage());
        }
    }


    private void handleSendFailedMessage(FileObject targetFileMessage, String domain, String messageId, String errorDetail) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
             FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);) {

            if (targetFileMessage != null) {
                String baseName = targetFileMessage.getName().getBaseName();
                String errorFileName = FSFileNameHelper.stripStatusSuffix(baseName) + BackendFSImpl.ERROR_EXTENSION;

                String targetFileMessageURI = targetFileMessage.getParent().getName().getPath();
                String failedDirectoryLocation = FSFileNameHelper.deriveFailedDirectoryLocation(targetFileMessageURI);
                FileObject failedDirectory = fsFilesManager.getEnsureChildFolder(rootDir, failedDirectoryLocation);

                try {
                    if (fsPluginProperties.isFailedActionDelete(domain)) {
                        // Delete
                        fsFilesManager.deleteFile(targetFileMessage);
                        LOG.debug("Send failed message file [{}] was deleted", messageId);
                    } else if (fsPluginProperties.isFailedActionArchive(domain)) {
                        // Archive
                        String archivedFileName = FSFileNameHelper.stripStatusSuffix(baseName);
                        FileObject archivedFile = failedDirectory.resolveFile(archivedFileName);
                        fsFilesManager.moveFile(targetFileMessage, archivedFile);
                        LOG.debug("Send failed message file [{}] was archived into [{}]", messageId, archivedFile.getName().getURI());
                    }
                } finally {
                    // Create error file
                    createErrorFile(errorDetail, errorFileName, failedDirectory);
                }
            } else {
                LOG.error("The send failed message file [{}] was not found in domain [{}]", messageId, domain);
            }
        } catch (IOException e) {
            throw new FSPluginException("Error handling the send failed message file " + messageId, e);
        }
    }


    private void createErrorFile(String errorMessage, String errorFileName, FileObject failedDirectory) throws IOException {
        String content = String.valueOf(getErrorFileContent(errorMessage));
        fsFilesManager.createFile(failedDirectory, errorFileName, content);
    }

    private StringBuilder getErrorFileContent(String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("errorDetail: ").append(errorMessage).append(LS);
        sb.append("mshRole: ").append(MSHRole.SENDING).append(LS);
        sb.append("timestamp: ").append(new Date()).append(LS);
        return sb;
    }

    private void renameProcessedFile(FileObject processableFile, String messageId) {
        String newFileName = FSFileNameHelper.deriveFileName(processableFile.getName().getBaseName(), messageId);
        
        try {
            fsFilesManager.renameFile(processableFile, newFileName);
        } catch(FileSystemException ex) {
            throw new FSPluginException("Error renaming file [" + processableFile.getName().getURI() + "] to [" + newFileName + "]", ex);
        }
    }

    private UserMessage parseMetadata(FileObject metadataFile) throws JAXBException, FileSystemException {
        return FSXMLHelper.parseXML(metadataFile.getContent().getInputStream(), UserMessage.class);
    }

}
