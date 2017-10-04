package eu.domibus.plugin.webService;

import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.AgreementRef;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.CollaborationInfo;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Service;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import eu.domibus.plugin.webService.generated.SendRequest;
import eu.domibus.plugin.webService.generated.SendResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by feriaad on 02/02/2016.
 */
@Ignore
public class SendMessageIT extends AbstractSendMessageIT {

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
     * Sample example of a test for the backend sendMessage service
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageOK() throws SendMessageFault, InterruptedException, SQLException {

        String payloadHref = "sbdh-order";
        SendRequest sendRequest = createSendRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        // Must use tc3 and TC3Leg1
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("TC3Leg1");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setValue("EDELIVERY-1110");
        collaborationInfo.setAgreementRef(agreementRef);
        Service service = new Service();
        service.setValue("bdx:noprocess");
        service.setType("tc3");
        collaborationInfo.setService(service);
        ebMSHeaderInfo.getUserMessage().setCollaborationInfo(collaborationInfo);

        super.prepareSendMessage("validAS4Response.xml");

        SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        verifySendMessageAck(response);
    }
}
