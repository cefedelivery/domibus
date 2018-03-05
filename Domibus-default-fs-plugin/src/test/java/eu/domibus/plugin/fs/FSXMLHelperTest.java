package eu.domibus.plugin.fs;

import eu.domibus.plugin.fs.ebms3.*;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * JUnit for {@link FSXMLHelper} class
 *
 * @author Catalin Enache
 * @version 1.0
 * @since 3.3.2
 */
public class FSXMLHelperTest {

    private FileObject testFolder;


    @Before
    public void setUp() throws Exception {
        FileSystemManager fileSystemManager = VFS.getManager();

        testFolder = fileSystemManager.resolveFile("ram:///FSXMLHelperTest");
        testFolder.createFolder();
    }

    @After
    public void tearDown() throws Exception {
        testFolder.delete();
        testFolder.close();
    }

    @Test
    public void testParseXML() throws IOException, JAXBException {
        FileObject metadataFile;
        final String partyIdType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";

        try (InputStream testMetadata = FSTestHelper.getTestResource(this.getClass(), "testParseXML_metadata.xml")) {
            metadataFile = testFolder.resolveFile("test_metadata.xml");
            metadataFile.createFile();
            FileContent metadataFileContent = metadataFile.getContent();
            IOUtils.copy(testMetadata, metadataFileContent.getOutputStream());
            metadataFile.close();
        }

        //tested method
        UserMessage userMessage = FSXMLHelper.parseXML(metadataFile.getContent().getInputStream(), UserMessage.class);
        Assert.assertNotNull(userMessage);
        Assert.assertEquals(preparePartyId("domibus-blue", partyIdType), userMessage.getPartyInfo().getFrom().getPartyId());
        Assert.assertEquals(preparePartyId("domibus-red", partyIdType), userMessage.getPartyInfo().getTo().getPartyId());
    }

    @Test
    public void testWriteXML() throws Exception {



        try (FileObject file = testFolder.resolveFile(FSSendMessagesService.METADATA_FILE_NAME);
             FileContent fileContent = file.getContent()) {

            //tested method
            FSXMLHelper.writeXML(fileContent.getOutputStream(), UserMessage.class, prepareUserMessage());

            Assert.assertNotNull(file);
            Assert.assertTrue(FSSendMessagesService.METADATA_FILE_NAME.equals(file.getName().getBaseName()));
        }
        Assert.assertEquals(prepareUserMessage(), FSTestHelper.getUserMessage(testFolder.resolveFile(FSSendMessagesService.METADATA_FILE_NAME).getContent().getInputStream()));

    }


    private UserMessage prepareUserMessage() {
        UserMessage userMessage = new UserMessage();
        final String partyIdType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";

        PartyInfo partyInfo = new PartyInfo();

        //from
        From from = new From();
        from.setPartyId(preparePartyId("domibus-blue", partyIdType));

        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        partyInfo.setFrom(from);

        //to
        To to = new To();
        to.setPartyId(preparePartyId("domibus-red", partyIdType));
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        //collaboration info
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        Service service = new Service();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        collaborationInfo.setService(service);
        collaborationInfo.setAction("TC1Leg1");
        userMessage.setCollaborationInfo(collaborationInfo);

        //message properties
        MessageProperties messageProperties = new MessageProperties();
        prepareProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", messageProperties);
        prepareProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4", messageProperties);
        userMessage.setMessageProperties(messageProperties);

        return userMessage;
    }

    private void prepareProperty(final String name, final String value, MessageProperties messageProperties) {
        Property originalSenderPropertry = new Property();
        originalSenderPropertry.setName(name);
        originalSenderPropertry.setValue(value);
        messageProperties.getProperty().add(originalSenderPropertry);
    }

    private PartyId preparePartyId(final String value, final String type) {
        PartyId partyIdFrom = new PartyId();
        partyIdFrom.setValue(value);
        partyIdFrom.setType(type);
        return partyIdFrom;
    }

}