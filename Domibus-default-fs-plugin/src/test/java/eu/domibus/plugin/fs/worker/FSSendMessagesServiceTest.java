package eu.domibus.plugin.fs.worker;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.JAXBException;

import mockit.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.BackendFSImpl;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.FSMessage;
import eu.domibus.plugin.fs.FSPluginProperties;
import eu.domibus.plugin.fs.FSTestHelper;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.vfs.FileObjectDataSource;
import mockit.integration.junit4.JMockit;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@RunWith(JMockit.class)
public class FSSendMessagesServiceTest {
    
    @Tested
    private FSSendMessagesService instance;
    
    @Injectable
    private FSPluginProperties fsPluginProperties;
    
    @Injectable
    private BackendFSImpl backendFSPlugin;
    
    @Injectable
    private FSFilesManager fsFilesManager;
    
    @Tested
    @Injectable
    private FSProcessFileService fsProcessFileService;
    
    private FileObject rootDir;
    private FileObject outgoingFolder;
    private FileObject contentFile;
    private FileObject metadataFile;
    
    private UserMessage metadata;
    
    @Before
    public void setUp() throws FileSystemException, IOException, JAXBException {
        String location = "ram:///FSSendMessagesServiceTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        outgoingFolder = rootDir.resolveFile(FSFilesManager.OUTGOING_FOLDER);
        outgoingFolder.createFolder();
        
        metadata = FSTestHelper.getUserMessage(this.getClass(), "testSendMessages_metadata.xml");
        
        try (InputStream testMetadata = FSTestHelper.getTestResource(this.getClass(), "testSendMessages_metadata.xml")) {
            metadataFile = outgoingFolder.resolveFile("metadata.xml");
            metadataFile.createFile();
            FileContent metadataFileContent = metadataFile.getContent();
            IOUtils.copy(testMetadata, metadataFileContent.getOutputStream());
            metadataFile.close();
        }
        
        try (InputStream testContent = FSTestHelper.getTestResource(this.getClass(), "testSendMessages_content.xml")) {
            contentFile = outgoingFolder.resolveFile("content.xml");
            contentFile.createFile();
            FileContent contentFileContent = contentFile.getContent();
            IOUtils.copy(testContent, contentFileContent.getOutputStream());
            contentFile.close();
        }
    }

    @After
    public void tearDown() throws FileSystemException {
        rootDir.close();
        outgoingFolder.close();
    }

    @Test
    public void testSendMessages() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsPluginProperties.getDomains();
            result = Collections.emptyList();
            
            fsFilesManager.setUpFileSystem(null);
            result = rootDir;
            
            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;
            
            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[] { metadataFile, contentFile };
            
            fsFilesManager.resolveSibling(contentFile, "metadata.xml");
            result = metadataFile;
            
            fsFilesManager.getDataHandler(contentFile);
            result = new DataHandler(new FileObjectDataSource(contentFile));
            
            backendFSPlugin.submit(with(new Delegate<FSMessage>() {
                 void delegate(FSMessage message) throws IOException {
                     Assert.assertNotNull(message);
                     Assert.assertNotNull(message.getDataHandlers());
                     DataHandler dataHandler = message.getDataHandlers().get("cid:message");
                     Assert.assertNotNull(dataHandler);
                     Assert.assertNotNull(message.getMetadata());

                     DataSource dataSource = dataHandler.getDataSource();
                     Assert.assertNotNull(dataSource);
                     Assert.assertEquals("content.xml", dataSource.getName());
                     Assert.assertTrue(
                             IOUtils.contentEquals(dataSource.getInputStream(), contentFile.getContent().getInputStream())
                     );

                     Assert.assertEquals(metadata, message.getMetadata());
                 }
            }));
            result = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";
        }};
        
        
        instance.sendMessages();
        
        new VerificationsInOrder(1) {{
            fsFilesManager.renameFile(contentFile, "content_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.xml");
        }};
    }

    @Test
    public void testSendMessages_Domain1_BadConfiguration() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsPluginProperties.getDomains();
            result = Collections.singletonList("DOMAIN1");

            fsFilesManager.setUpFileSystem("DOMAIN1");
            result = new FSSetUpException("Test-forced exception");
        }};

        instance.sendMessages();

        new Verifications() {{
            backendFSPlugin.submit(withAny(new FSMessage(null, null)));
            maxTimes = 0;
        }};
    }

    @Test()
    public void testSendMessages_MetaDataException() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsPluginProperties.getDomains();
            result = Collections.emptyList();

            fsFilesManager.setUpFileSystem(null);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[] { contentFile };

            fsFilesManager.resolveSibling(contentFile, "metadata.xml");
            result = rootDir.resolveFile("nonexistent_file");
        }};

        instance.sendMessages();

        new Verifications() {{
            backendFSPlugin.submit(withAny(new FSMessage(null, null)));
            maxTimes = 0;
        }};
    }
    
    @Test
    public void testSendMessages_RenameException() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsPluginProperties.getDomains();
            result = Collections.emptyList();
            
            fsFilesManager.setUpFileSystem(null);
            result = rootDir;
            
            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;
            
            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[] { metadataFile, contentFile };
            
            fsFilesManager.resolveSibling(contentFile, "metadata.xml");
            result = metadataFile;
            
            fsFilesManager.getDataHandler(contentFile);
            result = new DataHandler(new FileObjectDataSource(contentFile));
            
            backendFSPlugin.submit(with(new Delegate<FSMessage>() {
                 void delegate(FSMessage message) throws IOException {
                     Assert.assertNotNull(message);
                     Assert.assertNotNull(message.getDataHandlers());
                     DataHandler dataHandler = message.getDataHandlers().get("cid:message");
                     Assert.assertNotNull(dataHandler);
                     Assert.assertNotNull(message.getMetadata());

                     DataSource dataSource = dataHandler.getDataSource();
                     Assert.assertNotNull(dataSource);
                     Assert.assertEquals("content.xml", dataSource.getName());
                     Assert.assertTrue(
                             IOUtils.contentEquals(dataSource.getInputStream(), contentFile.getContent().getInputStream())
                     );

                     Assert.assertEquals(metadata, message.getMetadata());
                 }
            }));
            result = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";
            
            fsFilesManager.renameFile(contentFile, "content_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.xml");
            result = new FileSystemException("Test-forced exception");
        }};
        
        instance.sendMessages();
    }
    
}
