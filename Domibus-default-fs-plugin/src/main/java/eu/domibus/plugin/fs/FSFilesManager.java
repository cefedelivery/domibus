package eu.domibus.plugin.fs;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.vfs.FileObjectDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
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

    private static final String FTP_PREFIX = "ftp:";
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
        
        /*
         * This is a workaround for a VFS issue regarding FTP servers on Linux.
         * See https://issues.apache.org/jira/browse/VFS-620
         * Disabling this property forces usage of paths starting at the root
         * of the filesystem which sidesteps the problem.
         * We apply only to FTP URLs since the property applies to SFTP too but
         * that protocol works as intended.
         */
        if (location.startsWith(FTP_PREFIX)) {
            FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
        }

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
        
        forceLastModifiedTimeIfSupported(newFile);

        return newFile;
    }

    public void moveFile(FileObject file, FileObject targetFile) throws FileSystemException {
        file.moveTo(targetFile);
        
        forceLastModifiedTimeIfSupported(targetFile);
    }

    private void forceLastModifiedTimeIfSupported(FileObject file) throws FileSystemException {
        if (file.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
            try (FileContent fileContent = file.getContent()) {
                long currentTimeMillis = System.currentTimeMillis();
                fileContent.setLastModifiedTime(currentTimeMillis);
            }
        }
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
     * Creates a file in the directory with the given file name and content.
     *
     * @param directory base directory
     * @param fileName file name
     * @param content content
     * @throws java.io.IOException
     */
    public void createFile(FileObject directory, String fileName, String content) throws IOException {
        try (FileObject file = directory.resolveFile(fileName);
             OutputStream fileOS = file.getContent().getOutputStream();
             OutputStreamWriter fileOSW = new OutputStreamWriter(fileOS)) {
            
            fileOSW.write(content);
        }
    }

}
