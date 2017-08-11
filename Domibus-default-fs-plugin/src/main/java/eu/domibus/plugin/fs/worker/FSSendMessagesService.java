package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.ebms3.ObjectFactory;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;



/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSSendMessagesService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSSendMessagesService.class);
    
    private static final String METADATA_FILE_NAME = "metadata.xml";

    @Autowired
    private FSPluginProperties fsPluginProperties;
    
    @Resource(name = "backendFSPlugin")
    private BackendFSImpl backendFSPlugin;
    
    @Autowired
    private FSFilesManager fsFilesManager;
    
    /**
     * Triggering the purge means that the message files from the SENT directory 
     * older than X seconds will be removed
     */
    public void sendMessages() {
        LOG.debug("Sending file system messages...");
        
        sendMessages(null);
        
        for (String domain : fsPluginProperties.getDomains()) {
            sendMessages(domain);
        }
    }
    
    private void sendMessages(String domain) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER)) {
            
            FileObject[] contentFiles = fsFilesManager.findAllDescendantFiles(outgoingFolder);
            LOG.debug(Arrays.toString(contentFiles));

            List<FileObject> processableFiles = filterProcessableFiles(contentFiles);
            for (FileObject processableFile : processableFiles) {
                processFile(processableFile);
            }

            fsFilesManager.closeAll(contentFiles);
        } catch (FileSystemException ex) {
            LOG.error("Error sending messages", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        }
    }

    private void processFile(FileObject processableFile) {
        try (FileObject metadataFile = fsFilesManager.resolveSibling(processableFile, METADATA_FILE_NAME)) {
            if (metadataFile.exists()) {
                UserMessage metadata = parseMetadata(metadataFile);
                LOG.debug("{}: Metadata found and valid", processableFile.getName());

                DataHandler dataHandler = fsFilesManager.getDataHandler(processableFile);
                FSMessage message= new FSMessage(dataHandler, metadata);
                String messageId = backendFSPlugin.submit(message);
                LOG.debug("{}: Message submitted successfully", processableFile.getName());

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

    private void renameProcessedFile(FileObject processableFile, String messageId) throws FileSystemException {
        String newFileName = FSFileNameHelper.deriveFileName(processableFile.getName().getBaseName(), messageId);
        
        fsFilesManager.renameFile(processableFile, newFileName);
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

    private UserMessage parseMetadata(FileObject metadataFile) throws JAXBException, FileSystemException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller um = jaxbContext.createUnmarshaller();
        StreamSource streamSource = new StreamSource(metadataFile.getContent().getInputStream());
        JAXBElement<UserMessage> jaxbElement = um.unmarshal(streamSource, UserMessage.class);

        return jaxbElement.getValue();
    }

}
