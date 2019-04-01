package eu.domibus.plugin.fs.worker;

import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.fs.vfs.FileObjectDataSource;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FSProcessFileServiceTest {

    @Tested
    FSProcessFileService fsProcessFileService;

    @Injectable
    private FSFilesManager fsFilesManager;

    @Injectable
    private BackendFSImpl backendFSPlugin;

    @Injectable
    private FSPluginProperties fsPluginProperties;


    private String domain = null;

    private FileObject rootDir;
    private FileObject outgoingFolder;
    private FileObject contentFile;
    private FileObject metadataFile;

    private UserMessage metadata;

    @Before
    public void setUp() throws IOException, JAXBException {
        String location = "ram:///FSProcessFileServiceTest";

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
    public void test_processFile_FileExists_Success() throws Exception {
        final String messageId = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";

        new Expectations(1, fsProcessFileService) {{

            fsPluginProperties.getPayloadId(null);
            result = "cid:message";

            fsFilesManager.resolveSibling(contentFile, "metadata.xml");
            result = metadataFile;

            fsFilesManager.getDataHandler(contentFile);
            result = new DataHandler(new FileObjectDataSource(contentFile));

            backendFSPlugin.submit(with(new Delegate<FSMessage>() {
                void delegate(FSMessage message) throws IOException {
                    Assert.assertNotNull(message);
                    Assert.assertNotNull(message.getPayloads());
                    FSPayload fsPayload = message.getPayloads().get("cid:message");
                    Assert.assertNotNull(fsPayload);
                    Assert.assertNotNull(fsPayload.getDataHandler());
                    Assert.assertNotNull(message.getMetadata());

                    DataSource dataSource = fsPayload.getDataHandler().getDataSource();
                    Assert.assertNotNull(dataSource);
                    Assert.assertEquals("content.xml", dataSource.getName());
                    Assert.assertTrue(
                            IOUtils.contentEquals(dataSource.getInputStream(), contentFile.getContent().getInputStream())
                    );

                    Assert.assertEquals(metadata, message.getMetadata());
                }
            }));
            result = messageId;
        }};

        //tested method
        fsProcessFileService.processFile(contentFile, domain);

        new VerificationsInOrder(1) {{
            String newFileName;
            fsFilesManager.renameFile(contentFile, newFileName = withCapture());
            Assert.assertEquals("content_" + messageId + ".xml", newFileName);
        }};
    }

    @Test
    public void test_renameProcessedFile_Exception(final @Mocked FileObject processableFile) throws Exception {
        final String messageId = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";
        final String newFileName = "content_" + messageId + ".xml";

        new Expectations() {{

            fsFilesManager.renameFile(contentFile, newFileName);
            result = new FileSystemException("Unable to rename the file");
        }};

        try {
            //tested method
            fsProcessFileService.renameProcessedFile(contentFile, messageId);
            Assert.fail("exception expected");
        } catch (Exception e) {
            Assert.assertEquals(FSPluginException.class, e.getClass());
        }
    }


    @Test()
    public void test_processFile_MetaDataException(final @Mocked FileObject processableFile, final @Mocked FileObject metadataFile) throws Exception {

        new Expectations(1, fsProcessFileService) {{
            fsFilesManager.resolveSibling(processableFile, FSSendMessagesService.METADATA_FILE_NAME);
            result = metadataFile;

            metadataFile.exists();
            result = false;

            processableFile.getName().getURI();
            result = "nonexistent_file";
        }};

        fsProcessFileService.processFile(processableFile, domain);

        new Verifications() {{
            backendFSPlugin.submit(withAny(new FSMessage(null, null)));
            maxTimes = 0;
        }};
    }
}