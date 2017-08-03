package eu.domibus.plugin.fs.worker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSPluginProperties;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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

import eu.domibus.common.MessageStatus;
import eu.domibus.plugin.fs.ebms3.ObjectFactory;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSMetadataException;



/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSSendMessagesService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSSendMessagesService.class);
    
    private static final List<String> STATE_SUFFIXES;
    
    static {
        List<String> tempStateSuffixes = new LinkedList<>();
        for (MessageStatus status : MessageStatus.values()) {
            tempStateSuffixes.add(status.name());
        }
        
        STATE_SUFFIXES = Collections.unmodifiableList(tempStateSuffixes);
    }

    @Resource(name = "fsPluginProperties")
    private FSPluginProperties fsPluginProperties;
    
    
    
    
    
    
    
    
    
    /*
    * FIXME: Code to help development
    * MUST be removed
    */
    private static final String DEFAULT_PROPERTIES_PATH = "./src/main/resources/fsPlugin.properties";
    private FSSendMessagesService() throws IOException {
        fsPluginProperties = new FSPluginProperties();
        fsPluginProperties.load(new FileInputStream(DEFAULT_PROPERTIES_PATH));
    }
    public static void main(String args[]) throws IOException {
        FSSendMessagesService me = new FSSendMessagesService();
        me.sendMessages();
    }
    
    
    
    
    
    

    /**
     * Triggering the purge means that the message files from the SENT directory 
     * older than X seconds will be removed
     */
    public void sendMessages() {
        LOG.debug("Sending file system messages...");
        
        sendMessages(fsPluginProperties.getLocation(), null);
        
        for (String domain : fsPluginProperties.getDomains()) {
            sendMessages(fsPluginProperties.getLocation(domain), domain);
        }
    }
    
    private void sendMessages(String location, String domain) {
        try {
            FileObject rootDir;
            if (domain != null) {
                rootDir = setUpFileSystem(domain);
            } else {
                rootDir = setUpFileSystem();
            }
            
            FileObject[] contentFiles = rootDir.findFiles(new FileTypeSelector(FileType.FILE));
            
            LOG.debug(Arrays.toString(contentFiles));
            
            List<FileObject> processableFiles = filterProcessableFiles(contentFiles);
            
            for (FileObject processableFile : processableFiles) {
                try {
                    processFile(processableFile);
                } catch (FSMetadataException ex) {
                    LOG.error(null, ex);
                }
            }
        } catch (FileSystemException ex) {
            LOG.error(null, ex);
        }
    }

    private void processFile(FileObject processableFile) throws FileSystemException, FSMetadataException {
        FileObject metadataFile = processableFile.resolveFile("../metadata.xml");
        
        if (metadataFile.exists()) {
            try {
                UserMessage metadata = parseMetadata(metadataFile);
                
                LOG.debug("{}: Metadata found and valid", processableFile.getName());
            } catch (JAXBException | FileSystemException ex) {
                throw new FSMetadataException("Metadata file is not an XML file", ex);
            }
        } else {
            throw new FSMetadataException("Metadata file is missing");
        }
    }

    private List<FileObject> filterProcessableFiles(FileObject[] files) {
        List<FileObject> filteredFiles = new LinkedList<>();
        
        for (FileObject file : files) {
            String baseName = file.getName().getBaseName();
            
            if (!StringUtils.equals(baseName, "metadata.xml")) {
                if (!StringUtils.endsWithAny(baseName, STATE_SUFFIXES.toArray(new String[0]))) {
                    filteredFiles.add(file);
                }
            }
        }
        
        return filteredFiles;
    }

    private FileObject setUpFileSystem(String domain) throws FileSystemException {
        StaticUserAuthenticator auth = new StaticUserAuthenticator(null,
                fsPluginProperties.getUser(domain), fsPluginProperties.getPassword(domain));
        FileSystemOptions opts = new FileSystemOptions();
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        
        FileSystemManager fsManager = VFS.getManager();
        FileObject rootDir = fsManager.resolveFile(fsPluginProperties.getLocation(domain), opts);
        
        return rootDir;
    }
    
    private FileObject setUpFileSystem() throws FileSystemException {        
        FileSystemManager fsManager = VFS.getManager();
        FileObject rootDir = fsManager.resolveFile(fsPluginProperties.getLocation());
        
        return rootDir;
    }

    private UserMessage parseMetadata(FileObject metadataFile) throws JAXBException, FileSystemException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller um = jaxbContext.createUnmarshaller();
        StreamSource streamSource = new StreamSource(metadataFile.getContent().getInputStream());
        JAXBElement<UserMessage> jaxbElement = um.unmarshal(streamSource, UserMessage.class);

        return jaxbElement.getValue();
    }

}
