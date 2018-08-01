package eu.domibus.plugin.webService;

import eu.domibus.AbstractBackendWSIT;
import eu.domibus.common.services.MessagingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.webService.generated.MessageStatus;
import eu.domibus.plugin.webService.generated.StatusFault;
import eu.domibus.plugin.webService.generated.StatusRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;

@DirtiesContext
@Rollback
public class GetStatusIT extends AbstractBackendWSIT {

    @Autowired
    MessagingService messagingService;

    @Autowired
    Provider<SOAPMessage> mshWebservice;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void testGetStatusReceived() throws StatusFault, IOException, SOAPException, SAXException, ParserConfigurationException {
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = createSOAPMessage(filename);
        mshWebservice.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        StatusRequest messageStatusRequest = createMessageStatusRequest(messageId);
        MessageStatus response = backendWebService.getStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetStatusInvalidId() throws StatusFault {
        String invalidMessageId = "invalid";
        StatusRequest messageStatusRequest = createMessageStatusRequest(invalidMessageId);
        MessageStatus response = backendWebService.getStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.NOT_FOUND, response);
    }

    @Test(expected = StatusFault.class)
    public void testGetStatusEmptyMessageId() throws StatusFault {
        String emptyMessageId = "";
        StatusRequest messageStatusRequest = createMessageStatusRequest(emptyMessageId);
        try {
            MessageStatus response = backendWebService.getStatus(messageStatusRequest);
        } catch (StatusFault statusFault) {
            String message = "Status request is not valid against the XSD";
            Assert.assertEquals(message, statusFault.getMessage());
            throw statusFault;
        }
    }

    private StatusRequest createMessageStatusRequest(final String messageId) {
        StatusRequest statusRequest = new StatusRequest();
        statusRequest.setMessageID(messageId);
        return statusRequest;
    }
}
