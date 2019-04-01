package eu.domibus.plugin.fs.worker;


import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.BackendFSImpl;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.FSPluginProperties;
import eu.domibus.plugin.fs.FSTestHelper;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.Queue;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno, Catalin Enache
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

    @Injectable
    private AuthenticationExtService authenticationExtService;

    @Injectable
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    private FSMultiTenancyService fsMultiTenancyService;

    @Injectable
    private JMSExtService jmsExtService;

    @Injectable
    @Qualifier("fsPluginSendQueue")
    private Queue fsPluginSendQueue;

    @Tested
    @Injectable
    private FSProcessFileService fsProcessFileService;

    private FileObject rootDir;
    private FileObject outgoingFolder;
    private FileObject contentFile;
    private FileObject metadataFile;

    private UserMessage metadata;

    @Before
    public void setUp() throws IOException, JAXBException {
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
    public void test_SendMessages_Root_Domain1() {
        final String domain1 = "DOMAIN1";
        new Expectations(instance) {{
            fsPluginProperties.getDomains();
            result = Collections.singletonList(domain1);

            fsMultiTenancyService.verifyDomainExists(domain1);
            result = true;

        }};

        //tested method
        instance.sendMessages();

        new FullVerifications(instance) {{
            instance.sendMessages(null);

            instance.sendMessages(domain1);
        }};
    }

    @Test
    public void testSendMessages_RootDomain_NoMultitenancy() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        final String domain = null; //root
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = false;

            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[]{metadataFile, contentFile};
        }};

        //tested method
        instance.sendMessages(domain);

        new VerificationsInOrder(1) {{
            FileObject fileActual;
            String domainActual;
            instance.sendJMSMessageToOutQueue(fileActual = withCapture(), domainActual = withCapture());
            Assert.assertEquals(contentFile, fileActual);
            Assert.assertEquals(domain, domainActual);
        }};
    }

    @Test
    public void test_SendMessages_RootDomain_Multitenancy() throws FileSystemException, FSSetUpException {
        final String domain = null; //root
        final String domainDefault = FSSendMessagesService.DEFAULT_DOMAIN;
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;

            fsPluginProperties.getAuthenticationUser(domainDefault);
            result = "user1";

            fsPluginProperties.getAuthenticationPassword(domainDefault);
            result = "pass1";

            fsFilesManager.setUpFileSystem(domainDefault);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[]{metadataFile, contentFile};
        }};

        //tested method
        instance.sendMessages(domain);

        new VerificationsInOrder(1) {{
            authenticationExtService.basicAuthenticate(anyString, anyString);

            FileObject fileActual;
            String domainActual;
            instance.sendJMSMessageToOutQueue(fileActual = withCapture(), domainActual = withCapture());
            Assert.assertEquals(contentFile, fileActual);
            Assert.assertEquals(domainDefault, domainActual);
        }};
    }

    @Test
    public void testSendMessages_Domain1() throws MessagingProcessingException, FileSystemException {
        final String domain1 = "DOMAIN1";
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;

            fsFilesManager.setUpFileSystem(domain1);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[]{metadataFile, contentFile};

            fsPluginProperties.getAuthenticationUser(anyString);
            result = "user1";

            fsPluginProperties.getAuthenticationPassword(anyString);
            result = "pass1";

        }};

        instance.sendMessages(domain1);

        new Verifications() {{
            authenticationExtService.basicAuthenticate(anyString, anyString);

            FileObject fileActual;
            String domainActual;
            instance.sendJMSMessageToOutQueue(fileActual = withCapture(), domainActual = withCapture());
            Assert.assertEquals(contentFile, fileActual);
            Assert.assertEquals(domain1, domainActual);
        }};
    }

    @Test
    public void testSendMessages_Domain1_BadConfiguration() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        final String domain1 = "DOMAIN1";
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;

            fsPluginProperties.getAuthenticationUser(anyString);
            result = "user1";

            fsPluginProperties.getAuthenticationPassword(anyString);
            result = "pass1";

            fsFilesManager.setUpFileSystem("DOMAIN1");
            result = new FSSetUpException("Test-forced exception");
        }};

        instance.sendMessages(domain1);

        new Verifications() {{
            authenticationExtService.basicAuthenticate(anyString, anyString);

            instance.sendJMSMessageToOutQueue((FileObject)any, anyString);
            maxTimes = 0;
        }};
    }

}
