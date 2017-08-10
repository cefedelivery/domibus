package eu.domibus.plugin.fs;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.MessageLister;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSRuntimeException;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
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
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    
    private FileObject outgoingFolder;

    @Before
    public void setUp() throws org.apache.commons.vfs2.FileSystemException {
        String location = "ram:///BackendFSImplTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        incomingFolder = rootDir.resolveFile(FSFilesManager.INCOMING_FOLDER);
        incomingFolder.createFolder();
        
        outgoingFolder = rootDir.resolveFile(FSFilesManager.OUTGOING_FOLDER);
        outgoingFolder.createFolder();
    }

    @After
    public void tearDown() throws FileSystemException {
        rootDir.close();
        incomingFolder.close();
        outgoingFolder.close();
    }


    @Test
    public void testDeliverMessage_NormalFlow(@Injectable final FSMessage fsMessage)
            throws MessageNotFoundException, JAXBException, IOException, FSSetUpException {

        final String messageId = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";
        final String payloadContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        final DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(payloadContent.getBytes(), TEXT_XML));
        final UserMessage userMessage = FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml");

        new Expectations(1, backendFS) {{
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

    @Test(expected = FSRuntimeException.class)
    public void testDeliverMessage_MessageNotFound(@Injectable final FSMessage fsMessage) throws MessageNotFoundException {

        final String messageId = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";

        new Expectations(1, backendFS) {{
            backendFS.downloadMessage(messageId, null);
            result = new MessageNotFoundException("message not found");
        }};

        backendFS.deliverMessage(messageId);
    }

    @Test
    public void testGetMessageSubmissionTransformer() {
        MessageSubmissionTransformer<FSMessage> result = backendFS.getMessageSubmissionTransformer();
        
        Assert.assertEquals(defaultTransformer, result);
    }

    @Test
    public void testGetMessageRetrievalTransformer() {
        MessageRetrievalTransformer<FSMessage> result = backendFS.getMessageRetrievalTransformer();
        
        Assert.assertEquals(defaultTransformer, result);
    }

    @Test
    public void testMessageStatusChanged() throws FSSetUpException, FileSystemException {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        event.setMessageId("3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");
        event.setFromStatus(MessageStatus.READY_TO_SEND);
        event.setToStatus(MessageStatus.SEND_ENQUEUED);
        event.setChangeTimestamp(new Timestamp(new Date().getTime()));
        
        final FileObject contentFile = outgoingFolder.resolveFile("content_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.xml.READY_TO_SEND");
        
        new Expectations(1, backendFS) {{
//            unneeded when main location contains file
//            fsPluginProperties.getDomains();
//            result = Collections.emptySet();
            
            fsFilesManager.setUpFileSystem(null);
            result = rootDir;
            
            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;
            
            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[] { contentFile };
        }};
        
        backendFS.messageStatusChanged(event);
        
        contentFile.close();
        
        new VerificationsInOrder(1) {{
            fsFilesManager.renameFile(contentFile, "content_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.xml.SEND_ENQUEUED");
        }};
    }

    @Test
    public void testResolveDomain_1() {
        String serviceDomain1 = "ODRDocumentInvoiceService123";
        String actionDomain1 = "PrintA";

        final Set<String> domains = new HashSet<>();
        domains.add("DOMAIN1");

        new Expectations(1, backendFS) {{
            fsPluginProperties.getDomains();
            result = domains;

            fsPluginProperties.getExpression("DOMAIN1");
            // TODO confirm requirements ODRDocumentInvoiceService.*#Print.?
            result = "ODRDocumentInvoiceService.*#Print.?";
        }};

        String result = backendFS.resolveDomain(serviceDomain1, actionDomain1);
        Assert.assertEquals("DOMAIN1", result);
    }

    @Test
    public void testResolveDomain_2() {
        String serviceDomain2 = "BRISReceptionService";
        String actionDomain2 = "SendEmailAction";
        String actionDomain2a = "ReceiveBillAction";

        final Set<String> domains = new HashSet<>();
        domains.add("DOMAIN1");
        domains.add("DOMAIN2");

        new Expectations(1, backendFS) {{
            fsPluginProperties.getDomains();
            result = domains;

            fsPluginProperties.getExpression("DOMAIN2");
            result = "BRISReceptionService#.*";
        }};

        String result = backendFS.resolveDomain(serviceDomain2, actionDomain2);
        Assert.assertEquals("DOMAIN2", result);

        result = backendFS.resolveDomain(serviceDomain2, actionDomain2a);
        Assert.assertEquals("DOMAIN2", result);
    }

    @Test
    public void testResolveDomain_WithoutMatch() {
        String serviceDomain1 = "ODRDocumentInvoiceService123";
        String actionDomain1 = "PrintA";

        String serviceWithoutMatch = "FSService123";
        String actionWithoutMatch = "SomeAction";

        final Set<String> domains = new HashSet<>();
        domains.add("DOMAIN1");
        domains.add("DOMAIN2");

        new Expectations(1, backendFS) {{
            fsPluginProperties.getDomains();
            result = domains;

            fsPluginProperties.getExpression("DOMAIN1");
            result = "ODRDocumentInvoiceService.*#Print.?";

            fsPluginProperties.getExpression("DOMAIN2");
            result = "BRISReceptionService#.*";
        }};

        String result = backendFS.resolveDomain(serviceWithoutMatch, actionWithoutMatch);
        Assert.assertNull(result);

        result = backendFS.resolveDomain(serviceDomain1, actionWithoutMatch);
        Assert.assertNull(result);

        result = backendFS.resolveDomain(serviceWithoutMatch, actionDomain1);
        Assert.assertNull(result);
    }

    @Test
    public void testResolveDomain_bdxNoprocessTC1Leg1() {
        String service = "bdx:noprocess";
        String action = "TC1Leg1";

        final Set<String> domains = new HashSet<>();
        domains.add("DOMAIN1");

        new Expectations(1, backendFS) {{
            fsPluginProperties.getDomains();
            result = domains;

            fsPluginProperties.getExpression("DOMAIN1");
            result = "bdx:noprocess#TC1Leg1";
        }};

        String result = backendFS.resolveDomain(service, action);
        Assert.assertEquals("DOMAIN1", result);
    }

}
