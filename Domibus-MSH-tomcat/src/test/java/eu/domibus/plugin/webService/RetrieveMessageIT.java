package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.webService.generated.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.ConnectionFactory;
import javax.xml.ws.Holder;
import java.io.IOException;

public class RetrieveMessageIT extends AbstractIT {

    protected static ConnectionFactory connectionFactory;
    private static boolean initialized;

    @Autowired
    BackendInterface backendWebService;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            insertDataset("downloadMessage.sql");
            initialized = true;
            connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        }
    }

    @Test(expected = DownloadMessageFault.class)
    public void testMessageIdEmpty() throws DownloadMessageFault {
        retrieveMessageFail("", "MessageId is empty");
    }

    @Test(expected = DownloadMessageFault.class)
    public void testMessageNotFound() throws Exception {
        retrieveMessageFail("notFound", "No message with id [notFound] pending for download");
    }

    @Test
    @Transactional
    public void testMessageIdNeedsATrim() throws Exception {
        retrieveMessage("    2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu ");
    }

    @Test
    @Transactional
    public void testRetrieveMessageOk() throws Exception {
        retrieveMessage("2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu");
    }

    private void retrieveMessageFail(String messageId, String errorMessage) throws DownloadMessageFault {
        RetrieveMessageRequest retrieveMessageRequest = createRetrieveMessageRequest(messageId);

        Holder<RetrieveMessageResponse> retrieveMessageResponse = new Holder<>();
        Holder<Messaging> ebMSHeaderInfo = new Holder<>();

        try {
            backendWebService.retrieveMessage(retrieveMessageRequest, retrieveMessageResponse, ebMSHeaderInfo);
        } catch (DownloadMessageFault re) {
            Assert.assertEquals(errorMessage, re.getFaultInfo().getMessage());
            throw re;
        }
        Assert.fail("DownloadMessageFault was expected but was not raised");
    }

    private void retrieveMessage(String messageId) throws Exception {
        ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection("domibus", "changeit");
        pushMessage(connection, StringUtils.trim(messageId));

        RetrieveMessageRequest retrieveMessageRequest = createRetrieveMessageRequest(messageId);
        Holder<RetrieveMessageResponse> retrieveMessageResponse = new Holder<>();
        Holder<Messaging> ebMSHeaderInfo = new Holder<>();

        try {
            backendWebService.retrieveMessage(retrieveMessageRequest, retrieveMessageResponse, ebMSHeaderInfo);
        } catch (DownloadMessageFault dmf) {
            String message = "Downloading message failed";
            Assert.assertEquals(message, dmf.getMessage());
            throw dmf;
        }
        Assert.assertFalse(retrieveMessageResponse.value.getPayload().isEmpty());
        LargePayloadType payloadType = retrieveMessageResponse.value.getPayload().iterator().next();
        String payload = IOUtils.toString(payloadType.getValue().getDataSource().getInputStream());
        System.out.println("Payload returned [" + payload +"]");
        Assert.assertEquals(payload, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<hello>world</hello>");
    }

    private void pushMessage(ActiveMQConnection connection, String messageId) throws Exception {
        connection.start();
        pushQueueMessage(messageId, connection, WS_NOT_QUEUE);
        connection.close();
    }

    private RetrieveMessageRequest createRetrieveMessageRequest(String messageId) {
        RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        retrieveMessageRequest.setMessageID(messageId);
        return retrieveMessageRequest;
    }
}
