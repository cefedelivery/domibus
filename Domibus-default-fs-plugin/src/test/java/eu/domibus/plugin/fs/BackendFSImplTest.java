package eu.domibus.plugin.fs;

import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.MessageLister;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@RunWith(JMockit.class)
public class BackendFSImplTest {

    private static final String TEXT_XML = "text/xml";

    @Injectable
    protected MessageRetriever<Submission> messageRetriever;

    @Injectable
    protected MessageSubmitter<Submission> messageSubmitter;

    @Injectable
    private MessageLister lister;

    @Injectable
    private FSFilesManager fsFilesManager;

    @Injectable
    private FSPluginProperties fsPluginProperties;

    @Injectable
    private FSMessageTransformer defaultTransformer;

    @Injectable
    String name = "fsplugin";

    @Tested
    BackendFSImpl backendFS;

    private FileObject rootDir;

    private FileObject incomingFolder;

    @Before
    public void setUp() throws org.apache.commons.vfs2.FileSystemException {
        String location = "ram:///BackendFSImplTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        incomingFolder = rootDir.resolveFile(FSFilesManager.INCOMING_FOLDER);
        incomingFolder.createFolder();
    }

    @After
    public void tearDown() throws FileSystemException {
        rootDir.close();
        incomingFolder.close();
    }


    @Test
    public void testDeliverMessageNormalFlow(@Injectable final FSMessage fsMessage)
            throws MessageNotFoundException, JAXBException, IOException, FSSetUpException {

        final String messageId = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";
        final String payloadContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        final DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(payloadContent.getBytes(), TEXT_XML));
        final UserMessage userMessage = FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow", "metadata.xml");

        new Expectations(backendFS) {{
            backendFS.downloadMessage(messageId, null);
            result = new FSMessage(dataHandler, userMessage);

            fsFilesManager.getEnsureChildFolder(withAny(rootDir), FSFilesManager.INCOMING_FOLDER);
            result = incomingFolder;
        }};

        backendFS.deliverMessage(messageId);

        // Assert results
        FileObject[] files = incomingFolder.findFiles(new FileTypeSelector(FileType.FILE));
        Assert.assertEquals(1, files.length);
        FileObject fileMessage = files[0];

        Assert.assertEquals(messageId + ".xml", fileMessage.getName().getBaseName());
        Assert.assertEquals(payloadContent, IOUtils.toString(fileMessage.getContent().getInputStream()));
    }

}
