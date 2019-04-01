package eu.domibus.plugin.fs.worker;

import eu.domibus.common.MSHRole;
import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.FSFileNameHelper;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.FSPluginProperties;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSSendMessagesService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSSendMessagesService.class);

    public static final String METADATA_FILE_NAME = "metadata.xml";
    public static final String DEFAULT_DOMAIN = "default";
    public static final String ERROR_EXTENSION = ".error";
    private static final String LS = System.lineSeparator();

    @Autowired
    private FSPluginProperties fsPluginProperties;
    
    @Autowired
    private FSFilesManager fsFilesManager;
    
    @Autowired
    private FSProcessFileService fsProcessFileService;

    @Autowired
    private AuthenticationExtService authenticationExtService;

    @Autowired
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    private FSMultiTenancyService fsMultiTenancyService;

    @Autowired
    private JMSExtService jmsExtService;

    @Autowired
    @Qualifier("fsPluginSendQueue")
    private Queue fsPluginSendQueue;

    /**
     * Triggering the send messages means that the message files from the OUT directory
     * will be processed to be sent
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendMessages() {
        LOG.debug("Sending file system messages...");

        sendMessages(null);
        
        for (String domain : fsPluginProperties.getDomains()) {
            if (fsMultiTenancyService.verifyDomainExists(domain)) {
                sendMessages(domain);
            }
        }
    }

    protected void sendMessages(String domain) {
        if(domibusConfigurationExtService.isMultiTenantAware()) {
            if(domain == null) {
                domain = DEFAULT_DOMAIN;
            }

            String authenticationUser = fsPluginProperties.getAuthenticationUser(domain);
            if(authenticationUser == null) {
                LOG.error("Authentication User not defined for domain [{}]", domain);
                return;
            }

            String authenticationPassword = fsPluginProperties.getAuthenticationPassword(domain);
            if(authenticationPassword == null) {
                LOG.error("Authentication Password not defined for domain [{}]", domain);
                return;
            }

            authenticationExtService.basicAuthenticate(authenticationUser, authenticationPassword);
        }

        FileObject[] contentFiles = null;
        final String domainCode = domain;
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER)) {

            contentFiles = fsFilesManager.findAllDescendantFiles(outgoingFolder);
            LOG.trace("{}", contentFiles);

            List<FileObject> processableFiles = filterProcessableFiles(contentFiles);
            LOG.debug("Processable files [{}]", processableFiles);

            processableFiles.parallelStream().forEach(file -> sendJMSMessageToOutQueue(file, domainCode));

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

    /**
     * process the file - to be called by JMS message listener
     * @param processableFile
     * @param domain
     */
    public void processFileSafely(FileObject processableFile, String domain) {
        String errorMessage = null;
        try {
            fsProcessFileService.processFile(processableFile, domain);
        } catch (JAXBException ex) {
            errorMessage = buildErrorMessage("Invalid metadata file: " + ex.toString()).toString();
            LOG.error(errorMessage, ex);
        } catch (MessagingProcessingException ex) {
            errorMessage = buildErrorMessage("Error occurred submitting message to Domibus: " + ex.getMessage()).toString();
            LOG.error(errorMessage, ex);
        } catch (RuntimeException | FileSystemException ex) {
            errorMessage = buildErrorMessage("Error processing file. Skipped it. Error message is: " + ex.getMessage()).toString();
            LOG.error(errorMessage, ex);
        } finally {
            if(errorMessage != null) {
                handleSendFailedMessage(processableFile, domain, errorMessage);
            }
        }
    }

    public void handleSendFailedMessage(FileObject processableFile, String domain, String errorMessage) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain)) {
             if (processableFile != null) {
                String baseName = processableFile.getName().getBaseName();
                String errorFileName = FSFileNameHelper.stripStatusSuffix(baseName) + ERROR_EXTENSION;

                String processableFileMessageURI = processableFile.getParent().getName().getPath();
                String failedDirectoryLocation = FSFileNameHelper.deriveFailedDirectoryLocation(processableFileMessageURI);
                FileObject failedDirectory = fsFilesManager.getEnsureChildFolder(rootDir, failedDirectoryLocation);

                try {
                    if (fsPluginProperties.isFailedActionDelete(domain)) {
                        // Delete
                        fsFilesManager.deleteFile(processableFile);
                        LOG.debug("Send failed message file [{}] was deleted", processableFile.getName().getBaseName());
                    } else if (fsPluginProperties.isFailedActionArchive(domain)) {
                        // Archive
                        String archivedFileName = FSFileNameHelper.stripStatusSuffix(baseName);
                        FileObject archivedFile = failedDirectory.resolveFile(archivedFileName);
                        fsFilesManager.moveFile(processableFile, archivedFile);
                        LOG.debug("Send failed message file [{}] was archived into [{}]", processableFile, archivedFile.getName().getURI());
                    }
                } finally {
                    // Create error file
                    fsFilesManager.createFile(failedDirectory, errorFileName, errorMessage);
                }
            } else {
                LOG.error("The send failed message file [{}] was not found in domain [{}]", processableFile, domain);
            }
        } catch (IOException e) {
            throw new FSPluginException("Error handling the send failed message file " + processableFile, e);
        }
    }

    public StringBuilder buildErrorMessage(String errorDetail) {
        return buildErrorMessage(null, errorDetail, null, null, null, null);
    }

    public StringBuilder buildErrorMessage(String errorCode, String errorDetail, String messageId, String mshRole, String notified, String timestamp) {
        StringBuilder sb = new StringBuilder();
        if (errorCode != null) {
            sb.append("errorCode: ").append(errorCode).append(LS);
        }
        sb.append("errorDetail: ").append(errorDetail).append(LS);
        if(messageId != null) {
            sb.append("messageInErrorId: ").append(messageId).append(LS);
        }
        if(mshRole != null) {
            sb.append("mshRole: ").append(mshRole).append(LS);
        } else {
            sb.append("mshRole: ").append(MSHRole.SENDING).append(LS);
        }
        if(notified != null) {
            sb.append("notified: ").append(notified).append(LS);
        }
        if(timestamp != null) {
            sb.append("timestamp: ").append(timestamp).append(LS);
        } else {
            sb.append("timestamp: ").append(LocalDateTime.now()).append(LS);
        }

        return sb;
    }


    private List<FileObject> filterProcessableFiles(FileObject[] files) {
        List<FileObject> filteredFiles = new LinkedList<>();

        // locked file names
        List<String> lockedFileNames = Arrays.stream(files)
                .map(f -> f.getName().getBaseName())
                .filter(fname -> FSFileNameHelper.isLockFile(fname))
                .map(fname -> FSFileNameHelper.stripLockSuffix(fname))
                .collect(Collectors.toList());

        for (FileObject file : files) {
            String baseName = file.getName().getBaseName();

            if (!StringUtils.equals(baseName, METADATA_FILE_NAME)
                    && !FSFileNameHelper.isAnyState(baseName)
                    && !FSFileNameHelper.isProcessed(baseName)
                    // exclude lock files:
                    && !FSFileNameHelper.isLockFile(baseName)
                    // exclude locked files:
                    && !lockedFileNames.stream().anyMatch(fname -> fname.equals(baseName))
                    // exclude files that are (or could be) in use by other processes:
                    && canReadFileSafely(file)) {

                filteredFiles.add(file);
            }
        }

        return filteredFiles;
    }


    protected boolean canReadFileSafely(FileObject fileObject) {
        String filePath = fileObject.getName().getPath();

        // firstly try to lock the file
        // if this fails, it means that another process has an explicit lock on the file

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel fileChannel = raf.getChannel();
             FileLock lock = fileChannel.tryLock(0, 0, true)) {
            if (lock == null) {
                LOG.debug("Could not acquire lock on file [{}] ", filePath);
                return false;
            }
        } catch (Exception e) {
            LOG.debug("Could not acquire lock on file [{}] ", filePath, e);
            return false; // something else went wrong
        }

        // if the file timestamp is very recent,
        // it is probable that some process is still writing in the file

        try {
            long delta = 25000L; // 25s // TODO read from fs-plugin.properties
            long fileTime = fileObject.getContent().getLastModifiedTime();
            long currentTime = new java.util.Date().getTime();
            if (fileTime > currentTime - delta) {
                LOG.debug("Could not read file [{}] because it is too recent: [{}] ms", filePath, currentTime - fileTime);
                return false;
            }
        } catch (FileSystemException e) {
            LOG.warn("Could not determine file date for file [{}] ", filePath, e);
            return false;
        }

        return true;
    }

    /**
     * Put a JMS message to FS Plugin Send queue
     *
     * @param processableFile
     * @param domain
     */
    protected void sendJMSMessageToOutQueue(final FileObject processableFile, final String domain) {

        String fileName;
        try {
             fileName = processableFile.getURL().getFile();
        } catch (FileSystemException e) {
            LOG.error("Exception while getting filename: ", e);
            return;
        }

        final JmsMessageDTO jmsMessage = JMSMessageDTOBuilder.
                create().
                property(MessageConstants.DOMAIN, domain).
                property(MessageConstants.FILE_NAME, fileName).
                build();

        LOG.debug("send message: [{}] to fsPluginSendQueue for file: [{}]", jmsMessage, fileName);
        jmsExtService.sendMessageToQueue(jmsMessage, fsPluginSendQueue);
    }

}
