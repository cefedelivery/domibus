package eu.domibus.plugin.fs;

import static eu.domibus.common.MessageStatus.*;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import eu.domibus.common.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.fs.ebms3.CollaborationInfo;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tika.mime.MimeTypeException;

import eu.domibus.plugin.fs.exception.FSSetUpException;

import javax.activation.DataHandler;

/**
 * File system backend integration plugin.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class BackendFSImpl extends AbstractBackendConnector<FSMessage, FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFSImpl.class);

    private static final String _ERROR = ".error";

    private static final Set<MessageStatus> SENDING_MESSAGE_STATUSES = EnumSet.of(
            READY_TO_SEND, SEND_ENQUEUED, SEND_IN_PROGRESS, WAITING_FOR_RECEIPT,
            WAITING_FOR_RETRY, SEND_ATTEMPT_FAILED
    );

    private static final Set<MessageStatus> SEND_SUCCESS_MESSAGE_STATUSES = EnumSet.of(
            ACKNOWLEDGED, ACKNOWLEDGED_WITH_WARNING
    );

    private static final Set<MessageStatus> SEND_FAILED_MESSAGE_STATUSES = EnumSet.of(
            SEND_FAILURE
    );

    // receiving statuses should be REJECTED, RECEIVED_WITH_WARNINGS, DOWNLOADED, DELETED, RECEIVED

    @Autowired
    private FSMessageTransformer defaultTransformer;
    
    @Autowired
    private FSFilesManager fsFilesManager;
    
    @Autowired
    private FSPluginProperties fsPluginProperties;
    
    private final Map<String, Pattern> domainPatternCache = new HashMap<>();

    /**
     * Creates a new <code>BackendFSImpl</code>.
     *
     * @param name Connector name
     */
    public BackendFSImpl(String name) {
        super(name);
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
        LOG.debug("Delivering File System Message [{}]", messageId);
        FSMessage fsMessage;

        // Download message
        try {
            fsMessage = downloadMessage(messageId, null);
        } catch (MessageNotFoundException e) {
            throw new FSPluginException("Unable to download message " + messageId, e);
        }

        // Persist message
        String domain = resolveDomain(fsMessage);
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject incomingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.INCOMING_FOLDER)) {
            
            boolean multiplePayloads = fsMessage.getDataHandlers().size() > 1;

            for (Map.Entry<String, DataHandler> entry : fsMessage.getDataHandlers().entrySet()) {
                DataHandler dataHandler = entry.getValue();
                String contentId  = entry.getKey();
                String fileName = getFileName(multiplePayloads, messageId, contentId, dataHandler);

                try (FileObject fileObject = incomingFolder.resolveFile(fileName);
                     FileContent fileContent = fileObject.getContent()) {
                    dataHandler.writeTo(fileContent.getOutputStream());
                }
            }
        } catch (IOException | FSSetUpException ex) {
            throw new FSPluginException("An error occurred persisting downloaded message " + messageId, ex);
        }
    }

    private String getFileName(boolean multiplePayloads, String messageId, String contentId, DataHandler dataHandler) {
        String fileName = messageId;
        if (multiplePayloads) {
            fileName += "_" + contentId.replaceFirst("cid:", "");
        }
        fileName += getFileNameExtension(dataHandler);
        return fileName;
    }

    private String getFileNameExtension(DataHandler dataHandler) {
        String extension = "";
        try {
            String mimeType = dataHandler.getContentType();
            extension = FSMimeTypeHelper.getExtension(mimeType);
        } catch (MimeTypeException ex) {
            LOG.warn("Error parsing MIME type", ex);
        }
        return extension;
    }

    private String resolveDomain(FSMessage fsMessage) {
        CollaborationInfo collaborationInfo = fsMessage.getMetadata().getCollaborationInfo();
        String service = collaborationInfo.getService().getValue();
        String action = collaborationInfo.getAction();
        return resolveDomain(service, action);
    }

    String resolveDomain(String service, String action) {
        String serviceAction = service + "#" + action;
        List<String> domains = fsPluginProperties.getDomains();
        for (String domain : domains) {
            Pattern domainExpressionPattern = getDomainPattern(domain);
            if (domainExpressionPattern != null) {
                boolean domainMatches = domainExpressionPattern.matcher(serviceAction).matches();
                if (domainMatches) {
                    return domain;
                }
            }
        }
        return null;
    }
    
    private Pattern getDomainPattern(String domain) {
        if (domainPatternCache.containsKey(domain)) {
            return domainPatternCache.get(domain);
        } else {
            String domainExpression = fsPluginProperties.getExpression(domain);
            Pattern domainExpressionPattern = null;
            if (StringUtils.isNotEmpty(domainExpression)) {
                try {
                    domainExpressionPattern = Pattern.compile(domainExpression);
                } catch (PatternSyntaxException e) {
                    LOG.warn("Invalid domain expression for " + domain, e);
                }
            }

            // domainExpressionPattern may be null, we should still cache null and return it
            domainPatternCache.put(domain, domainExpressionPattern);
            return domainExpressionPattern;
        }
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        // No-op
        // Probably, the AbstractBackendConnector should not throw the UnsupportedOperationException
    }

    @Override
    public void messageSendSuccess(String messageId) {
        // Implemented in messageStatusChanged to avoid event collision and use improved API
        // Probably, the AbstractBackendConnector should not throw the UnsupportedOperationException
    }

    @Override
    public void messageSendFailed(String messageId) {
        // Implemented in messageStatusChanged to avoid event collision and use improved API
        // Probably, the AbstractBackendConnector should implement a default no-op
    }

    private boolean handleSendFailedMessage(String messageId, String domain) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
             FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
             FileObject targetFileMessage = findMessageFile(outgoingFolder, messageId)) {

            if (targetFileMessage != null) {
                // TODO: Derive error filename
                String baseName = targetFileMessage.getName().getBaseName();
                String errorFileName = FSFileNameHelper.stripStatusSuffix(baseName) + _ERROR;

                String targetFileMessageURI = targetFileMessage.getParent().getName().getURI();
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
                    // TODO: check why the file is not created in the domain scenario
                    List<ErrorResult> errors = super.getErrorsForMessage(messageId);
                    fsFilesManager.createErrorFile(errorFileName, failedDirectory, errors);
                }
                return true;
            } else {
                LOG.error("The send failed file message file was not found. " + messageId);
            }
        } catch (IOException e){
            LOG.error("Error handling the send failed message file " + messageId, e);
        }
        return false;
    }

    private boolean handleSentMessage(String messageId, String domain) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
             FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
             FileObject targetFileMessage = findMessageFile(outgoingFolder, messageId)) {

            if (targetFileMessage != null) {
                if (fsPluginProperties.isSentActionDelete(domain)) {
                    //Delete
                    fsFilesManager.deleteFile(targetFileMessage);
                    LOG.debug("Successfully sent message file [{}] was deleted", messageId);
                } else if (fsPluginProperties.isSentActionArchive(domain)) {
                    // Archive
                    String targetFileMessageURI = targetFileMessage.getParent().getName().getURI();
                    String sentDirectoryLocation = FSFileNameHelper.deriveSentDirectoryLocation(targetFileMessageURI);
                    FileObject sentDirectory = fsFilesManager.getEnsureChildFolder(rootDir, sentDirectoryLocation);

                    String baseName = targetFileMessage.getName().getBaseName();
                    String newName = FSFileNameHelper.stripStatusSuffix(baseName);
                    FileObject archivedFile = sentDirectory.resolveFile(newName);
                    fsFilesManager.moveFile(targetFileMessage, archivedFile);

                    LOG.debug("Successfully sent message file [{}] was archived into [{}]", messageId, archivedFile.getName().getURI());
                }
                return true;
            } else {
                LOG.error("The successfully sent message file was not found. " + messageId);
            }
        } catch (FileSystemException e) {
            LOG.error("Error handling the successfully sent message file " + messageId, e);
        }
        return false;
    }

    private FileObject findMessageFile(FileObject parentDir, String messageId) throws FileSystemException {
        FileObject[] files = fsFilesManager.findAllDescendantFiles(parentDir);
        try {
            FileObject targetFile = null;
            for (FileObject file : files) {
                String baseName = file.getName().getBaseName();
                if (FSFileNameHelper.isMessageRelated(baseName, messageId)) {
                    targetFile = file;
                    break;
                }
            }
            return targetFile;
        } finally {
            fsFilesManager.closeAll(files);
        }
    }

    @Override
    public void messageStatusChanged(MessageStatusChangeEvent event) {
        String messageId = event.getMessageId();
        LOG.debug("Message [{}] changed status from [{}] to [{}]", messageId, event.getFromStatus(), event.getToStatus());

        if (isSendingEvent(event)) {
            handleSendingEvent(event, messageId);
        } else if (isSendSuccessEvent(event)) {
            handleSendSuccessEvent(messageId);
        } else if (isMessageSendFailedEvent(event)) {
            handleSendFailedEvent(messageId);
        }

    }

    private void handleSendFailedEvent(String messageId) {
        boolean messageFound = handleSendFailedMessage(messageId, null);
        if (!messageFound) {
            for (String domain : fsPluginProperties.getDomains()) {
                if (handleSendFailedMessage(messageId, domain)) {
                    break; // target file found
                }
            }
        }
    }

    private void handleSendSuccessEvent(String messageId) {
        boolean messageFound = handleSentMessage(messageId, null);
        if (!messageFound) {
            for (String domain : fsPluginProperties.getDomains()) {
                if (handleSentMessage(messageId, domain)) {
                    break; // target file found
                }
            }
        }
    }

    private void handleSendingEvent(MessageStatusChangeEvent event, String messageId) {
        boolean fileRenamed = renameMessageFile(null, messageId, event.getToStatus());
        if (!fileRenamed) {
            for (String domain : fsPluginProperties.getDomains()) {
                fileRenamed = renameMessageFile(domain, messageId, event.getToStatus());
                if (fileRenamed) {
                    break;
                }
            }
        }
    }

    private boolean isSendingEvent(MessageStatusChangeEvent event) {
        return SENDING_MESSAGE_STATUSES.contains(event.getToStatus());
    }

    private boolean isSendSuccessEvent(MessageStatusChangeEvent event) {
        return SEND_SUCCESS_MESSAGE_STATUSES.contains(event.getToStatus());
    }

    private boolean isMessageSendFailedEvent(MessageStatusChangeEvent event) {
        return SEND_FAILED_MESSAGE_STATUSES.contains(event.getToStatus());
    }

    private boolean renameMessageFile(String domain, String messageId, MessageStatus status) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
                FileObject targetFile = findMessageFile(outgoingFolder, messageId)) {
            
            if (targetFile != null) {
                String baseName = targetFile.getName().getBaseName();
                String newName = FSFileNameHelper.deriveFileName(baseName, status);
                fsFilesManager.renameFile(targetFile, newName);
                
                return true;
            } else {
                LOG.error("The message to rename was not found. " + messageId);
            }
        } catch (FileSystemException ex) {
            LOG.error("Error renaming file", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        }
        
        return false;
    }

}
