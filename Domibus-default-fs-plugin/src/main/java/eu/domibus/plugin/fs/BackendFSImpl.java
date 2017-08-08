package eu.domibus.plugin.fs;

import java.io.IOException;

import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tika.mime.MimeTypeException;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.plugin.fs.exception.FSSetUpException;

/**
 * File system backend integration plugin.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class BackendFSImpl extends AbstractBackendConnector<FSMessage, FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFSImpl.class);

    @Autowired
    private FSMessageTransformer defaultTransformer;
    
    @Autowired
    private FSFilesManager fsFilesManager;
    
    @Autowired
    private FSPluginProperties fsPluginProperties;

    /**
     * Creates a new <code>BackendFSImpl</code>.
     *
     * @param name Connector name
     */
    public BackendFSImpl(String name) {
        super(name);
    }

    @PostConstruct
    public void init() {
        LOG.info("The File System Plugin is initialized.");
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageSubmissionTransformer
     */
    @Override
    public MessageSubmissionTransformer<FSMessage> getMessageSubmissionTransformer() {
        return this.defaultTransformer;
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageRetrievalTransformer
     */
    @Override
    public MessageRetrievalTransformer<FSMessage> getMessageRetrievalTransformer() {
        return this.defaultTransformer;
    }

    @Override
    public void deliverMessage(String messageId) {
        LOG.debug("Delivering File System Message {}", messageId);
        FSMessage fsMessage;
        
        try {
            fsMessage = downloadMessage(messageId, null);
        } catch (MessageNotFoundException e) {
            LOG.error("An error occurred during message download", e);
            return;
        }
            
        try {
            FileObject rootDir = fsFilesManager.getEnsureRootLocation(fsPluginProperties.getLocation());
            FileObject incomingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.INCOMING_FOLDER);
            
            String fileName = messageId;
            try {
                String mimeType = fsMessage.getDataHandler().getContentType();
                String extension = FSMimeTypeHelper.getExtension(mimeType);

                fileName += extension;
            } catch (MimeTypeException ex) {
                LOG.warn("Error parsing MIME type", ex);
            }
            
            try (FileObject fileObject = incomingFolder.resolveFile(fileName);
                    FileContent fileContent = fileObject.getContent()) {
                fsMessage.getDataHandler().writeTo(fileContent.getOutputStream());
            }
        } catch (IOException | FSSetUpException ex) {
            LOG.error("An error occured saving downloaded message", ex);
        }
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageSendFailed(String messageId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageSendSuccess(String messageId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageStatusChanged(MessageStatusChangeEvent event) {
        LOG.debug("Message {} changed status from {} to {}", event.getMessageId(), event.getFromStatus(), event.getToStatus());
        
        boolean fileRenamed = renameMessageFile(null, event.getMessageId(), event.getToStatus());
        if (!fileRenamed) {
            for (String domain : fsPluginProperties.getDomains()) {
                fileRenamed = renameMessageFile(domain, event.getMessageId(), event.getToStatus());
                if (fileRenamed) {
                    break;
                }
            }
        }
    }
    
    private boolean renameMessageFile(String domain, String messageId, MessageStatus status) {
        try {
            FileObject rootDir;
            if (domain != null) {
                rootDir = setUpFileSystem(domain);
            } else {
                rootDir = setUpFileSystem();
            }
            
            // FIXME: remove magic string
            FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, "OUT");
            FileObject[] files = fsFilesManager.findAllDescendantFiles(outgoingFolder);
            
            FileObject targetFile = null;
            for (FileObject file : files) {
                if (file.getName().getBaseName().contains(messageId)) {
                    targetFile = file;
                    break;
                }
            }
            
            if (targetFile != null) {
                String baseName = targetFile.getName().getBaseName();
                String newName = FSFileNameHelper.deriveFileName(baseName, status);
                fsFilesManager.renameFile(targetFile, newName);
                
                return true;
            }
        } catch (FileSystemException ex) {
            LOG.error(null, ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        }
        
        return false;
    }
    
    // FIXME: this duplicates code in FSSendMessagesService
    private FileObject setUpFileSystem(String domain) throws FileSystemException, FSSetUpException {
        String location = fsPluginProperties.getLocation(domain);
        String authDomain = null;
        String user = fsPluginProperties.getUser(domain);
        String password = fsPluginProperties.getPassword(domain);
        
        FileObject rootDir;
        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            rootDir = fsFilesManager.getEnsureRootLocation(location);
        } else {
            rootDir = fsFilesManager.getEnsureRootLocation(location, authDomain, user, password);
        }
        
        return rootDir;
    }
    
    private FileObject setUpFileSystem() throws FileSystemException, FSSetUpException {        
        String location = fsPluginProperties.getLocation();
        FileObject rootDir = fsFilesManager.getEnsureRootLocation(location);
        
        return rootDir;
    }

}
