package eu.domibus.plugin.fs.queue;

import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.FSTestHelper;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Message;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FSSendMessageListenerTest {

    @Injectable
    private FSSendMessagesService fsSendMessagesService;

    @Tested
    FSSendMessageListener fsSendMessageListener;

    private FileObject rootDir;
    private FileObject outgoingFolder;
    private FileObject contentFile;

    @Before
    public void setUp() throws IOException {
        String location = "ram:///FSSendMessageListenerTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        outgoingFolder = rootDir.resolveFile(FSFilesManager.OUTGOING_FOLDER);
        outgoingFolder.createFolder();

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
    public void test_onMessage_FileExists_Success(final @Mocked FileSystemManager fileSystemManager, final @Mocked Message message, final @Mocked FileObject file) throws Exception {
        final String domain = null;
        final String fileName = "ram:" + contentFile.getURL().getFile();


        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = domain;

            message.getStringProperty(MessageConstants.FILE_NAME);
            result = fileName;
        }};

        //tested method
        fsSendMessageListener.onMessage(message);

        new FullVerifications(fsSendMessagesService) {{
            String domainActual;
            fsSendMessagesService.processFileSafely((FileObject) any, domainActual = withCapture());
            Assert.assertEquals(domain, domainActual);
        }};
    }

    @Test
    public void test_onMessage_FileDoesntExist_Error(final @Mocked FileSystemManager fileSystemManager, final @Mocked Message message, final @Mocked FileObject file) throws Exception {
        final String domain = null;
        final String fileName = "ram:" + contentFile.getURL().getFile() + "bla";


        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = domain;

            message.getStringProperty(MessageConstants.FILE_NAME);
            result = fileName;
        }};

        //tested method
        fsSendMessageListener.onMessage(message);

        new Verifications() {{
            fsSendMessagesService.processFileSafely((FileObject) any, anyString);
            maxTimes = 0;
        }};
    }
}