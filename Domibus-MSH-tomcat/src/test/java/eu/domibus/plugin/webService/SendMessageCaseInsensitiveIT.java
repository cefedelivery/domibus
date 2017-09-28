package eu.domibus.plugin.webService;

import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import eu.domibus.plugin.webService.generated.SendRequest;
import eu.domibus.plugin.webService.generated.SendResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author venugar
 * @since 3.3
 */

public class SendMessageCaseInsensitiveIT extends AbstractSendMessageIT {

    private static boolean initialized;

    @Autowired
    BackendInterface backendWebService;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageDataset.sql");
            initialized = true;
        }
    }

    /**
     * Sample example of a test for the backend sendMessage service.
     * The message components should be case insensitive from the PMode data
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageOK() throws SendMessageFault, SQLException, InterruptedException {

        String payloadHref = "SBDH-ORDER";
        SendRequest sendRequest = createSendRequest(payloadHref);

        super.prepareSendMessage("validAS4Response.xml");

        Messaging ebMSHeaderInfo = new Messaging();
        UserMessage userMessage = new UserMessage();
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("TC3LEG1");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setValue("edelivery-1110");
        collaborationInfo.setAgreementRef(agreementRef);
        Service service = new Service();
        service.setValue("BDX:NOPROCESS");
        service.setType("TC3");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(createProperty("originalSender", "URN:OASIS:NAMES:TC:EBCORE:PARTYID-TYPE:UNREGISTERED:C1", STRING_TYPE));
        messageProperties.getProperty().add(createProperty("finalRecipient", "URN:OASIS:NAMES:TC:EBCORE:PARTYID-TYPE:UNREGISTERED:C41", STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);
        PartyInfo partyInfo = new PartyInfo();
        From from = new From();
        from.setRole("HTTP://DOCS.OASIS-OPEN.ORG/EBXML-MSG/EBMS/V3.0/NS/CORE/200704/INITIATOR");
        PartyId sender = new PartyId();
        sender.setValue("URN:OASIS:NAMES:TC:EBCORE:PARTYID-TYPE:UNREGISTERED:DOMIBUS-BLUE");
        from.setPartyId(sender);
        partyInfo.setFrom(from);
        To to = new To();
        to.setRole("HTTP://DOCS.OASIS-OPEN.ORG/EBXML-MSG/EBMS/V3.0/NS/CORE/200704/RESPONDER");
        PartyId receiver = new PartyId();
        receiver.setValue("URN:OASIS:NAMES:TC:EBCORE:PARTYID-TYPE:UNREGISTERED:DOMIBUS-RED");
        to.setPartyId(receiver);
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        Description description = new Description();
        description.setValue("e-sens-sbdh-order");
        description.setLang("en-US");
        partInfo.setHref(payloadHref);
        String mimeType = "TEXT/XML";
        if (mimeType != null) {
            PartProperties partProperties = new PartProperties();
            partProperties.getProperty().add(createProperty(mimeType, "MimeType", STRING_TYPE));
            partInfo.setPartProperties(partProperties);
        }
        partInfo.setDescription(description);
        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        ebMSHeaderInfo.setUserMessage(userMessage);

        SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        verifySendMessageAck(response);
    }
}
