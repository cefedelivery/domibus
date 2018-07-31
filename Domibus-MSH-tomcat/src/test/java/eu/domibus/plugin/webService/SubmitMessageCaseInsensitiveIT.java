package eu.domibus.plugin.webService;

import eu.domibus.AbstractBackendWSIT;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author venugar
 * @since 3.3
 */
@DirtiesContext
@Rollback
public class SubmitMessageCaseInsensitiveIT extends AbstractBackendWSIT {


    @Autowired
    JMSManager jmsManager;

    @Before
    public void updatePMode() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    /**
     * Sample example of a test for the backend sendMessage service.
     * The message components should be case insensitive from the PMode data
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */


    @Test
    public void testSubmitMessageOK() throws SubmitMessageFault, SQLException, InterruptedException {
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);

        super.prepareSendMessage("validAS4Response.xml");

        final Messaging messaging = createMessageHeader(payloadHref);
        messaging.getUserMessage().getCollaborationInfo().setAction("TC3Leg1");

        SubmitResponse response = backendWebService.submitMessage(submitRequest, messaging);
        verifySendMessageAck(response);
    }
}
