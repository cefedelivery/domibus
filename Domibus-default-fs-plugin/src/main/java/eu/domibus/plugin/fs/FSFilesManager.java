package eu.domibus.plugin.fs;

import javax.activation.DataHandler;

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
import org.apache.commons.vfs2.util.FileObjectDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import org.springframework.stereotype.Component;

import eu.domibus.plugin.fs.exception.FSSetUpException;

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
    
    @Autowired
    private FSPluginProperties fsPluginProperties;

    public FileObject getEnsureRootLocation(final String location, final String domain,
            final String user, final String password) throws FileSystemException, FSSetUpException {
        StaticUserAuthenticator auth = new StaticUserAuthenticator(domain, user, password);
        FileSystemOptions opts = new FileSystemOptions();
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

        FileSystemManager fsManager = getVFSManager();
        FileObject rootDir = fsManager.resolveFile(location, opts);

        if (!rootDir.exists()) {
            throw new FSSetUpException("Root location does not exist: " + rootDir.getName());
        } else {
            return rootDir;
        }
    }

    public FileObject getEnsureRootLocation(final String location) throws FileSystemException, FSSetUpException {
        FileSystemManager fsManager = getVFSManager();
        FileObject rootDir = fsManager.resolveFile(location);

        if (!rootDir.exists()) {
            throw new FSSetUpException("Root location does not exist: " + rootDir.getName());
        } else {
            return rootDir;
        }
    }
    
    private FileSystemManager getVFSManager() throws FileSystemException {
        return VFS.getManager();
    }
    
    public FileObject getEnsureChildFolder(FileObject rootDir, String folderName) throws FSSetUpException {
        try {
            if (!rootDir.exists()) {
                throw new FSSetUpException("Root location does not exist: " + rootDir.getName());
            } else {
                FileObject outgoingDir = rootDir.resolveFile(folderName);
                if (!outgoingDir.exists()) {
                    outgoingDir.createFolder();
                } else {
                    if (outgoingDir.getType() != FileType.FOLDER) {
                        throw new FSSetUpException("Child path exists and is not a folder");
                    }
                }
                return outgoingDir;
            }
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
    
    public FileObject setUpFileSystem(String domain) throws FileSystemException, FSSetUpException {
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

}
