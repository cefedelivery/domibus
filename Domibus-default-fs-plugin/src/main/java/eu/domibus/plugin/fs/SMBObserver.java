package eu.domibus.plugin.fs;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * SMB share observer (PoC).
 * Watches for and acts upon file system events on the outgoing folder.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class SMBObserver {
    
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SMBObserver.class);
    
    private static final String OUTGOING_FOLDER_NAME = "OUT";
    private static final String SMB_DOMAIN = "SMB";
    
    @Resource(name = "fsPluginProperties")
    private FSPluginProperties fsPluginProperties;
    
    @Autowired
    @Qualifier("fsPluginObserverExecutor")
    private TaskExecutor taskExecutor;
    
    private DefaultFileMonitor fm;
    
    @PostConstruct
    public void init() {
        LOG.debug("SMBObserver has started");
        LOG.debug("SMBObserver Properties: {}", fsPluginProperties);
        
        setupListeners();
    }
    
    @PreDestroy
    public void destroy() {
        LOG.debug("SMBObserver is stoping");
        
        if (fm != null) {
            fm.stop();
        }
    }
    
    private void setupListeners() {
        try {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(null,
                    fsPluginProperties.getUser(SMB_DOMAIN), fsPluginProperties.getPassword(SMB_DOMAIN));
            FileSystemOptions opts = new FileSystemOptions();
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
            
            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(fsPluginProperties.getLocation(SMB_DOMAIN), opts);
            
            LOG.debug(Arrays.toString(listendir.getChildren()));
            
            fm = new DefaultFileMonitor(new CustomFileListener());
            fm.setRecursive(true);
            fm.addFile(listendir);
            // DefaultFileMonitor does not run on an executor just yet,
            // might require a few changes
//            taskExecutor.execute(fm);
            fm.start();
//
//        try {
//            Thread.sleep(15*1000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(NewMain1.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        fm.stop();
        } catch (FileSystemException ex) {
            LOG.error(null, ex);
        }

    }
    
    static class CustomFileListener implements FileListener {

        public CustomFileListener() {
        }

        @Override
        public void fileCreated(FileChangeEvent event) throws Exception {
            LOG.debug("File created {}", event.getFile());
        }

        @Override
        public void fileDeleted(FileChangeEvent event) throws Exception {
            LOG.debug("File deleted {}", event.getFile());
        }

        @Override
        public void fileChanged(FileChangeEvent event) throws Exception {
            LOG.debug("File changed {}", event.getFile());
        }
    }
    
}

