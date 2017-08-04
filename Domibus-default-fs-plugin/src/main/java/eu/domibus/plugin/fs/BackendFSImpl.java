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
import javax.annotation.Resource;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;

import eu.domibus.plugin.fs.exception.FSSetUpException;

/**
 * File system backend integration plugin.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class BackendFSImpl extends AbstractBackendConnector<FSMessage, FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFSImpl.class);
    
    private static final String INCOMING_FOLDER = "IN";
    private static final String DEFAULT_EXTENSION = "txt";
    
    @Autowired
    private FSMessageTransformer defaultTransformer;
    
    @Autowired
    private FSFilesManager fsFilesManager;
    
    @Resource(name = "fsPluginProperties")
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
            FileObject incomingFolder = fsFilesManager.getEnsureChildFolder(rootDir, INCOMING_FOLDER);
            String fileName = messageId + "." + DEFAULT_EXTENSION;
            
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

}
