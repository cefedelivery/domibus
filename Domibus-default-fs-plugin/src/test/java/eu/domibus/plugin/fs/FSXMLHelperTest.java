package eu.domibus.plugin.fs;

import eu.domibus.plugin.fs.ebms3.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

/**
 * JUnit for {@link FSXMLHelper} class
 *
 * @author Catalin Enache
 * @version 1.0
 * @since 28/02/2018
 */
@RunWith(JMockit.class)
public class FSXMLHelperTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void parseXML() {
    }

    @Test
    public void writeXML_FileIsWritten_NoExceptionThrown() throws  Exception {

        String fileNameToTest = folder.getRoot().getAbsolutePath() + "/metadata.xml";

        FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(fileNameToTest)) {

            //tested class method
            FSXMLHelper.writeXML(file.getContent().getOutputStream(), UserMessage.class, prepareUserMessage());

            Assert.assertNotNull(file);
        }
        Assert.assertTrue("file should exist", fileSystemManager.resolveFile(fileNameToTest).exists());
        Assert.assertTrue("file size should be > 0", fileSystemManager.resolveFile(fileNameToTest).getContent().getSize() > 0L);
    }

    @Test
    public void loadSchema() {
    }

    @Test
    public void loadSchema1() {
    }

    private UserMessage prepareUserMessage() {
        UserMessage userMessage = new UserMessage();

        PartyInfo partyInfo = new PartyInfo();

        //from
        From from = new From();
        from.setPartyId(preparePartyId("domibus-blue", "urn:oasis:names:tc:ebcore:partyid-type:unregistered"));

        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        partyInfo.setFrom(from);

        //to
        To to = new To();
        to.setPartyId(preparePartyId("domibus-red", "urn:oasis:names:tc:ebcore:partyid-type:unregistered"));
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        //TODO add Collaboration info

        return  userMessage;
    }

    private PartyId preparePartyId(final String value, final String type) {
        PartyId partyIdFrom = new PartyId();
        partyIdFrom.setValue(value);
        partyIdFrom.setType(type);
        return partyIdFrom;
    }

}