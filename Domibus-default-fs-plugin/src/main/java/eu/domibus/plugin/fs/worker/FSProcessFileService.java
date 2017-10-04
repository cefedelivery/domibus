package eu.domibus.plugin.fs.worker;

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.BackendFSImpl;
import eu.domibus.plugin.fs.FSFileNameHelper;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.FSMessage;
import eu.domibus.plugin.fs.FSPayload;
import eu.domibus.plugin.fs.FSXMLHelper;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSPluginException;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSProcessFileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSProcessFileService.class);

    private static final String DEFAULT_CONTENT_ID = "cid:message";
    
    @Resource(name = "backendFSPlugin")
    private BackendFSImpl backendFSPlugin;
    
    @Autowired
    private FSFilesManager fsFilesManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processFile(FileObject processableFile) {
        try (FileObject metadataFile = fsFilesManager.resolveSibling(processableFile, FSSendMessagesService.METADATA_FILE_NAME)) {
            if (metadataFile.exists()) {
                UserMessage metadata = parseMetadata(metadataFile);
                LOG.debug("Metadata found and valid: [{}]", processableFile.getName());

                DataHandler dataHandler = fsFilesManager.getDataHandler(processableFile);
                Map<String, FSPayload> fsPayloads = new HashMap<>(1);
                fsPayloads.put(DEFAULT_CONTENT_ID, new FSPayload(null, dataHandler));
                FSMessage message= new FSMessage(fsPayloads, metadata);
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
        } catch (MessagingProcessingException ex) {
            LOG.error("Error occurred submitting message to Domibus", ex);
        }
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
