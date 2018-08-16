package eu.domibus.plugin.fs;

import eu.domibus.common.*;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.MessageLister;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@RunWith(JMockit.class)
public class BackendFSImplTest {

    private static final String TEXT_XML = "text/xml";

    @Injectable
    protected MessageRetriever messageRetriever;

    @Injectable
    protected MessageSubmitter messageSubmitter;

    @Injectable
    private MessageLister lister;

    @Injectable
    private FSFilesManager fsFilesManager;

    @Injectable
    private FSPluginProperties fsPluginProperties;

    @Injectable
    private FSMessageTransformer defaultTransformer;

    @Injectable
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    private DomainExtService domainExtService;

    @Injectable
    private DomainContextExtService domainContextExtService;

    @Injectable
    private FSSendMessagesService fsSendMessagesService = new FSSendMessagesService();

    @Injectable
    String name = "fsplugin";

    @Tested
    BackendFSImpl backendFS;

    private FileObject rootDir;

    private FileObject incomingFolder;

    private FileObject incomingFolderByRecipient, incomingFolderByMessageId;
    
    private FileObject outgoingFolder;
    
    private FileObject sentFolder;

    private FileObject failedFolder;

    private final String location = "ram:///BackendFSImplTest";
    private final String messageId = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";
    private final String finalRecipientFolder = "urn_oasis_names_tc_ebcore_partyid-type_unregistered_C4";

    @Before
    public void setUp() throws org.apache.commons.vfs2.FileSystemException {
        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        incomingFolder = rootDir.resolveFile(FSFilesManager.INCOMING_FOLDER);
        incomingFolder.createFolder();


        incomingFolderByRecipient = incomingFolder.resolveFile(finalRecipientFolder);
        incomingFolderByRecipient.createFolder();

        incomingFolderByMessageId = incomingFolderByRecipient.resolveFile(messageId);
        incomingFolderByMessageId.createFolder();
        
        outgoingFolder = rootDir.resolveFile(FSFilesManager.OUTGOING_FOLDER);
        outgoingFolder.createFolder();
        
        sentFolder = rootDir.resolveFile(FSFilesManager.SENT_FOLDER);
        sentFolder.createFolder();

        failedFolder = rootDir.resolveFile(FSFilesManager.FAILED_FOLDER);
        failedFolder.createFolder();
    }

    @After
    public void tearDown() throws FileSystemException {
        incomingFolder.close();
        incomingFolderByRecipient.close();
        incomingFolderByMessageId.close();

        outgoingFolder.close();
        sentFolder.close();
        
        rootDir.deleteAll();
        rootDir.close();
    }

    @Test
    public void testDeliverMessage_NormalFlow(@Injectable final FSMessage fsMessage)
            throws MessageNotFoundException, JAXBException, IOException, FSSetUpException {

        final String payloadFileName = "message_test.xml";
        final String payloadContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        final DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(payloadContent.getBytes(), TEXT_XML));
        final UserMessage userMessage = FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml");
        final Map<String, FSPayload> fsPayloads = new HashMap<>();
        fsPayloads.put("cid:message", new FSPayload(TEXT_XML, payloadFileName, dataHandler));

        expectationsDeliverMessage(null, userMessage, fsPayloads);

        backendFS.deliverMessage(messageId);

        // Assert results
        FileObject[] files = incomingFolderByMessageId.findFiles(new FileTypeSelector(FileType.FILE));

        Assert.assertEquals(2, files.length);

        //metadata first
        FileObject metadataFile = files[0];

        Assert.assertEquals(FSSendMessagesService.METADATA_FILE_NAME, metadataFile.getName().getBaseName());
        Assert.assertEquals(FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml"),
                FSTestHelper.getUserMessage(metadataFile.getContent().getInputStream()));
        metadataFile.delete();
        metadataFile.close();

        //payload
        FileObject payloadFile = files[1];

        Assert.assertEquals(payloadFileName, payloadFile.getName().getBaseName());
        Assert.assertEquals(payloadContent, IOUtils.toString(payloadFile.getContent().getInputStream()));
        payloadFile.delete();
        payloadFile.close();
    }

    private void expectationsDeliverMessage(String domain, UserMessage userMessage, Map<String, FSPayload> fsPayloads) throws MessageNotFoundException, FileSystemException {
        new Expectations(1, backendFS) {{
            backendFS.downloadMessage(messageId, null);
            result = new FSMessage(fsPayloads, userMessage);

            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            domibusConfigurationExtService.isMultiTenantAware();
            result = (domain != null);

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.INCOMING_FOLDER);
            result = incomingFolder;

            fsFilesManager.getEnsureChildFolder(incomingFolder, finalRecipientFolder);
            result = incomingFolderByRecipient;

            fsFilesManager.getEnsureChildFolder(incomingFolderByRecipient, messageId);
            result = incomingFolderByMessageId;
        }};
    }

   /* @Test
    public void testDeliverMessage_Multitenancy(@Injectable final FSMessage fsMessage) throws JAXBException, MessageNotFoundException, FileSystemException {

        final UserMessage userMessage = FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml");
        final String messageContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGludm9pY2U+aGVsbG88L2ludm9pY2U+";
        final String invoiceContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";


        final DataHandler messageHandler = new DataHandler(new ByteArrayDataSource(messageContent.getBytes(), TEXT_XML));
        final DataHandler invoiceHandler = new DataHandler(new ByteArrayDataSource(invoiceContent.getBytes(), TEXT_XML));
        final Map<String, FSPayload> fsPayloads = new HashMap<>();
        fsPayloads.put("cid:message", new FSPayload(TEXT_XML, "message.xml", messageHandler));
        fsPayloads.put("cid:invoice", new FSPayload(TEXT_XML, "invoice.xml", invoiceHandler));

        expectationsDeliverMessage("DOMAIN1", userMessage, fsPayloads);

        backendFS.deliverMessage(messageId);
    }*/

    @Test
    public void testDeliverMessage_MultiplePayloads(@Injectable final FSMessage fsMessage)
            throws MessageNotFoundException, JAXBException, IOException, FSSetUpException {

        final UserMessage userMessage = FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml");
        final String messageContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGludm9pY2U+aGVsbG88L2ludm9pY2U+";
        final String invoiceContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";


        final DataHandler messageHandler = new DataHandler(new ByteArrayDataSource(messageContent.getBytes(), TEXT_XML));
        final DataHandler invoiceHandler = new DataHandler(new ByteArrayDataSource(invoiceContent.getBytes(), TEXT_XML));
        final Map<String, FSPayload> fsPayloads = new HashMap<>();
        fsPayloads.put("cid:message", new FSPayload(TEXT_XML, "message.xml", messageHandler));
        fsPayloads.put("cid:invoice", new FSPayload(TEXT_XML, "invoice.xml", invoiceHandler));

        expectationsDeliverMessage(null, userMessage, fsPayloads);

        //tested method
        backendFS.deliverMessage(messageId);

        // Assert results
        FileObject[] files = incomingFolderByMessageId.findFiles(new FileTypeSelector(FileType.FILE));
        Assert.assertEquals(3, files.length);

        FileObject fileMetadata = files[0];
        Assert.assertEquals(FSSendMessagesService.METADATA_FILE_NAME,
                fileMetadata.getName().getBaseName());
        Assert.assertEquals(FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml"),
                FSTestHelper.getUserMessage(fileMetadata.getContent().getInputStream()));
        fileMetadata.delete();
        fileMetadata.close();

        FileObject fileMessage0 = files[1];
        Assert.assertEquals("message.xml",
                fileMessage0.getName().getBaseName());
        Assert.assertEquals(messageContent, IOUtils.toString(fileMessage0.getContent().getInputStream()));
        fileMessage0.delete();
        fileMessage0.close();

        FileObject fileMessage1 = files[2];
        Assert.assertEquals("invoice.xml",
                fileMessage1.getName().getBaseName());
        Assert.assertEquals(invoiceContent, IOUtils.toString(fileMessage1.getContent().getInputStream()));
        fileMessage1.delete();
        fileMessage1.close();


    }

    @Test(expected = FSPluginException.class)
    public void testDeliverMessage_MessageNotFound(@Injectable final FSMessage fsMessage) throws MessageNotFoundException {

        new Expectations(1, backendFS) {{
            backendFS.downloadMessage(messageId, null);
            result = new MessageNotFoundException("message not found");
        }};

        backendFS.deliverMessage(messageId);
    }
    
    @Test(expected = FSPluginException.class)
    public void testDeliverMessage_FSSetUpException(@Injectable final FSMessage fsMessage)
            throws MessageNotFoundException, JAXBException, IOException, FSSetUpException {

        final String payloadContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        final DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(payloadContent.getBytes(), TEXT_XML));
        final UserMessage userMessage = FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml");
        final Map<String, FSPayload> fsPayloads = new HashMap<>();
        fsPayloads.put("cid:message", new FSPayload(TEXT_XML, "message.xml", dataHandler));

        new Expectations(1, backendFS) {{
            backendFS.downloadMessage(messageId, null);
            result = new FSMessage(fsPayloads, userMessage);

            fsFilesManager.setUpFileSystem(null);
            result = new FSSetUpException("Test-forced exception");
        }};

        backendFS.deliverMessage(messageId);
    }
    
    @Test(expected = FSPluginException.class)
    public void testDeliverMessage_IOException(@Injectable final FSMessage fsMessage)
            throws MessageNotFoundException, JAXBException, IOException, FSSetUpException {

        // the null causes an IOException
        final DataHandler dataHandler = new DataHandler(new ByteArrayDataSource((byte[])null, TEXT_XML));
        final UserMessage userMessage = FSTestHelper.getUserMessage(this.getClass(), "testDeliverMessageNormalFlow_metadata.xml");
        final Map<String, FSPayload> fsPayloads = new HashMap<>();
        fsPayloads.put("cid:message", new FSPayload(TEXT_XML, "message.xml", dataHandler));

        new Expectations(1, backendFS) {{
            backendFS.downloadMessage(messageId, null);
            result = new FSMessage(fsPayloads, userMessage);
            
            fsFilesManager.setUpFileSystem(null);
            result = rootDir;
            
            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.INCOMING_FOLDER);
            result = incomingFolder;
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
        event.setMessageId(messageId);
        event.setFromStatus(MessageStatus.READY_TO_SEND);
        event.setToStatus(MessageStatus.SEND_ENQUEUED);
        event.setChangeTimestamp(new Timestamp(new Date().getTime()));

        final FileObject contentFile = outgoingFolder.resolveFile("content_" + messageId + ".xml.READY_TO_SEND");
        
        new Expectations(1, backendFS) {{
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
            fsFilesManager.renameFile(contentFile, "content_" + messageId + ".xml.SEND_ENQUEUED");
        }};
    }

    @Test
    public void testResolveDomain_1() {
        String serviceDomain1 = "ODRDocumentInvoiceService123";
        String actionDomain1 = "PrintA";

        final List<String> domains = new ArrayList<>();
        domains.add("DOMAIN1");

        new Expectations(1, backendFS) {{
            fsPluginProperties.getDomains();
            result = domains;

            fsPluginProperties.getExpression("DOMAIN1");
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

        final List<String> domains = new ArrayList<>();
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

        final List<String> domains = new ArrayList<>();
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

        final List<String> domains = new ArrayList<>();
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

    @Test
    public void testMessageStatusChanged_SendSuccessDelete() throws FileSystemException {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        event.setMessageId(messageId);
        event.setFromStatus(MessageStatus.SEND_ENQUEUED);
        event.setToStatus(MessageStatus.ACKNOWLEDGED);
        event.setChangeTimestamp(new Timestamp(new Date().getTime()));

        final FileObject contentFile = outgoingFolder.resolveFile("content_" + messageId + ".xml.ACKNOWLEDGED");
        
        new Expectations(1, backendFS) {{
            fsFilesManager.setUpFileSystem(null);
            result = rootDir;
            
            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;
            
            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[] { contentFile };
            
            fsPluginProperties.isSentActionDelete(null);
            result = true;
        }};

        backendFS.messageStatusChanged(event);

        contentFile.close();
        
        new VerificationsInOrder(1) {{
            fsFilesManager.deleteFile(contentFile);
        }};
    }

    @Test
    public void testMessageStatusChanged_SendSuccessArchive() throws FileSystemException {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        event.setMessageId(messageId);
        event.setFromStatus(MessageStatus.SEND_ENQUEUED);
        event.setToStatus(MessageStatus.ACKNOWLEDGED);
        event.setChangeTimestamp(new Timestamp(new Date().getTime()));

        final FileObject contentFile = outgoingFolder.resolveFile("content_" + messageId + ".xml.ACKNOWLEDGED");
        
        new Expectations(1, backendFS) {{
            fsFilesManager.setUpFileSystem(null);
            result = rootDir;
            
            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;
            
            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[] { contentFile };
            
            fsPluginProperties.isSentActionDelete(null);
            result = false;
            
            fsPluginProperties.isSentActionArchive(null);
            result = true;
            
            fsFilesManager.getEnsureChildFolder(rootDir, "/BackendFSImplTest/SENT/");
            result = sentFolder;
            
        }};
        
        backendFS.messageStatusChanged(event);

        contentFile.close();
        
        new VerificationsInOrder(1) {{
            fsFilesManager.moveFile(contentFile, with(new Delegate<FileObject>() {
              void delegate(FileObject file) throws IOException {
                     Assert.assertNotNull(file);
                     Assert.assertEquals( location + "/SENT/content_" + messageId + ".xml", file.getName().getURI());
                 }  
            }));
        }};
    }

    @Test
    public void testMessageStatusChanged_SendFailedDelete() throws FileSystemException {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        event.setMessageId(messageId);
        event.setFromStatus(MessageStatus.SEND_ENQUEUED);
        event.setToStatus(MessageStatus.SEND_FAILURE);
        event.setChangeTimestamp(new Timestamp(new Date().getTime()));

        final FileObject contentFile = outgoingFolder.resolveFile("content_" + messageId + ".xml.SEND_ENQUEUED");

        new Expectations(1, backendFS) {{
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
            fsSendMessagesService.handleSendFailedMessage(contentFile, anyString, anyString);
        }};
    }

    @Test
    public void testMessageStatusChanged_SendFailedArchive() throws FileSystemException {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        event.setMessageId(messageId);
        event.setFromStatus(MessageStatus.SEND_ENQUEUED);
        event.setToStatus(MessageStatus.SEND_FAILURE);
        event.setChangeTimestamp(new Timestamp(new Date().getTime()));

        final FileObject contentFile = outgoingFolder.resolveFile("content_" + messageId + ".xml.SEND_ENQUEUED");

        new Expectations(1, backendFS) {{
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
            fsSendMessagesService.handleSendFailedMessage(contentFile, anyString, anyString);
        }};
    }

    @Test
    public void testMessageStatusChanged_SendFailedErrorFile() throws IOException {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        event.setMessageId(messageId);
        event.setFromStatus(MessageStatus.SEND_ENQUEUED);
        event.setToStatus(MessageStatus.SEND_FAILURE);
        event.setChangeTimestamp(new Timestamp(new Date().getTime()));

        final FileObject contentFile = outgoingFolder.resolveFile("content_" + messageId + ".xml.SEND_ENQUEUED");

        final List<ErrorResult> errorList = new ArrayList<>();
        ErrorResultImpl errorResult = new ErrorResultImpl();
        errorResult.setErrorCode(ErrorCode.EBMS_0001);
        errorList.add(errorResult);

        new Expectations(1, backendFS) {{
            fsFilesManager.setUpFileSystem(null);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[] { contentFile };

            backendFS.getErrorsForMessage(messageId);
            result = errorList;
        }};

        backendFS.messageStatusChanged(event);

        contentFile.close();

        new VerificationsInOrder(1) {{
            fsSendMessagesService.handleSendFailedMessage(contentFile, anyString, anyString);
        }};
    }

}
