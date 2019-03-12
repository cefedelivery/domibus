package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSPluginException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSProcessFileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSProcessFileService.class);

    @Resource(name = "backendFSPlugin")
    private BackendFSImpl backendFSPlugin;

    @Autowired
    private FSFilesManager fsFilesManager;

    @Autowired
    private FSPluginProperties fsPluginProperties;

    @Transactional(propagation = Propagation.SUPPORTS)
    public void processFile(FileObject processableFile, String domain) throws FileSystemException, JAXBException, MessagingProcessingException {

        try (FileObject metadataFile = fsFilesManager.resolveSibling(processableFile, FSSendMessagesService.METADATA_FILE_NAME)) {
            if (metadataFile.exists()) {
                UserMessage metadata = parseMetadata(metadataFile);
                LOG.debug("Metadata found and valid: [{}]", processableFile.getName());

                DataHandler dataHandler = fsFilesManager.getDataHandler(processableFile);
                Map<String, FSPayload> fsPayloads = new HashMap<>(1);

                //we add mimetype later, base name and dataHandler now
                String payloadId = fsPluginProperties.getPayloadId(domain);
                final FSPayload fsPayload = new FSPayload(null, processableFile.getName().getBaseName(), dataHandler);
                fsPayload.setFileSize(processableFile.getContent().getSize());
                fsPayload.setFilePath(processableFile.getURL().getPath());
                fsPayloads.put(payloadId, fsPayload);
                FSMessage message = new FSMessage(fsPayloads, metadata);
                String messageId = backendFSPlugin.submit(message);
                LOG.info("Message [{}] submitted: [{}]", messageId, processableFile.getName());

                fsFilesManager.createLockFile(processableFile);

            } else {
                LOG.error("Metadata file is missing for " + processableFile.getName().getURI());
            }
        }
    }

    public void renameProcessedFile(FileObject processableFile, String messageId) {
        final String baseName = processableFile.getName().getBaseName();
        String newFileName = FSFileNameHelper.deriveFileName(baseName, messageId);

        LOG.debug("Renaming file [{}] to [{}]", baseName, newFileName);

        try {
            fsFilesManager.renameFile(processableFile, newFileName);
        } catch (FileSystemException ex) {
            throw new FSPluginException("Error renaming file [" + processableFile.getName().getURI() + "] to [" + newFileName + "]", ex);
        }
    }

    private UserMessage parseMetadata(FileObject metadataFile) throws JAXBException, FileSystemException {
        return FSXMLHelper.parseXML(metadataFile.getContent().getInputStream(), UserMessage.class);
    }

}
