package eu.domibus.plugin.webService;

import eu.domibus.AbstractSendMessageIT;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author venugar
 * @since 3.3
 */

public class SubmitMessageCaseInsensitiveIT extends AbstractSendMessageIT {

    @Autowired
    BackendInterface backendWebService;

    @Autowired
    JMSManager jmsManager;

    @Before
    public void updatePMode() throws IOException, XmlProcessingException {
        final byte[] pmodeBytes = IOUtils.toByteArray(new ClassPathResource("dataset/pmode/PModeTemplate.xml").getInputStream());
        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(pmodeBytes);
        configurationDAO.updateConfiguration(pModeConfiguration);
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
