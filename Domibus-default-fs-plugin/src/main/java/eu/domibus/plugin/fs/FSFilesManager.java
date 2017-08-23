package eu.domibus.plugin.fs;

import javax.activation.DataHandler;

import eu.domibus.common.ErrorResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileTypeSelector;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import org.springframework.stereotype.Component;

import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.vfs.FileObjectDataSource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * This class is responsible for performing complex operations using VFS
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
@Component
public class FSFilesManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSFilesManager.class);
    
    private static final String PARENT_RELATIVE_PATH = "../";

    public static final String INCOMING_FOLDER = "IN";
    public static final String OUTGOING_FOLDER = "OUT";
    public static final String SENT_FOLDER = "SENT";
    public static final String FAILED_FOLDER = "FAILED";

    @Autowired
    private FSPluginProperties fsPluginProperties;

    public FileObject getEnsureRootLocation(final String location, final String domain,
            final String user, final String password) throws FileSystemException {
        StaticUserAuthenticator auth = new StaticUserAuthenticator(domain, user, password);
        FileSystemOptions opts = new FileSystemOptions();
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

        FileSystemManager fsManager = getVFSManager();
        FileObject rootDir = fsManager.resolveFile(location, opts);
        checkRootDirExists(rootDir);

        return rootDir;
    }

    private void checkRootDirExists(FileObject rootDir) throws FileSystemException {
        if (!rootDir.exists()) {
            throw new FSSetUpException("Root location does not exist: " + rootDir.getName());
        }
    }

    public FileObject getEnsureRootLocation(final String location) throws FileSystemException {
        FileSystemManager fsManager = getVFSManager();
        FileObject rootDir = fsManager.resolveFile(location);
        checkRootDirExists(rootDir);
        return rootDir;
    }
    
    private FileSystemManager getVFSManager() throws FileSystemException {
        return VFS.getManager();
    }
    
    public FileObject getEnsureChildFolder(FileObject rootDir, String folderName) {
        try {
            checkRootDirExists(rootDir);
            FileObject outgoingDir = rootDir.resolveFile(folderName);
            if (!outgoingDir.exists()) {
                outgoingDir.createFolder();
            } else {
                if (outgoingDir.getType() != FileType.FOLDER) {
                    throw new FSSetUpException("Child path exists and is not a folder");
                }
            }
            return outgoingDir;
        } catch (FileSystemException ex) {
            throw new FSSetUpException("IO error setting up folders", ex);
        }
    }
    
    public FileObject[] findAllDescendantFiles(FileObject folder) throws FileSystemException {
        return folder.findFiles(new FileTypeSelector(FileType.FILE));
    }
    
    public DataHandler getDataHandler(FileObject file) {
        return new DataHandler(new FileObjectDataSource(file));
    }
    
    public FileObject resolveSibling(FileObject file, String siblingName) throws FileSystemException {
        return file.resolveFile(PARENT_RELATIVE_PATH + siblingName);
    }

    public FileObject renameFile(FileObject file, String newFileName) throws FileSystemException {
        FileObject newFile = resolveSibling(file, newFileName);
        file.moveTo(newFile);

        return newFile;
    }

    public void moveFile(FileObject file, FileObject targetFile) throws FileSystemException {
        file.moveTo(targetFile);
    }

    public boolean deleteFile(FileObject file) throws FileSystemException {
        return file.delete();
    }

    public FileObject setUpFileSystem(String domain) throws FileSystemException {
        // Domain or default location
        String location = fsPluginProperties.getLocation(domain);
        String authDomain = null;
        String user = fsPluginProperties.getUser(domain);
        String password = fsPluginProperties.getPassword(domain);

        FileObject rootDir;
        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            rootDir = getEnsureRootLocation(location);
        } else {
            rootDir = getEnsureRootLocation(location, authDomain, user, password);
        }
        return rootDir;
    }
    
    public void closeAll(FileObject[] files) {
        for (FileObject file : files) {
            try {
                file.close();
            } catch (FileSystemException ex) {
                // errors with close are not very important at this point
                // just log in case there's an underlying problem
                LOG.warn("Error closing file", ex);
            }
        }
    }

    /**
     * TODO: When the message has failed to be sent from C2 to C3 and it will contain the failed reason in plain text.
     * This error file must contain the reason for the error, the date and time, the number of retries, and the stacktrace.
     * When possible, hints on how the issue can be solved should be added.
     *
     * @param targetFileMessage
     * @param failedDirectory
     * @param error
     */
    public void createErrorFile(FileObject targetFileMessage, FileObject failedDirectory, ErrorResult error)
            throws IOException {
        LOG.warn("Error sending message: " + error.getErrorDetail());
        String baseName = targetFileMessage.getName().getBaseName();
        String errorFileName = FSFileNameHelper.stripStatusSuffix(baseName) + ".error";

        try (FileObject errorFile = failedDirectory.resolveFile(errorFileName);
             OutputStream errorFileOS = errorFile.getContent().getOutputStream();
             OutputStreamWriter errorFileOSW = new OutputStreamWriter(errorFileOS)) {

            errorFile.createFile();
            errorFileOSW.write(error.getErrorDetail());
        }
    }

}
