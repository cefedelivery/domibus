package eu.domibus.plugin.webService;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.AbstractIT;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.ListPendingMessagesResponse;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.io.IOException;

/**
 * This JUNIT implements the Test cases List Pending Messages-01 and List Pending Messages-02.
 *
 * @author martifp
 */
public class PendingMessagesListIT extends AbstractIT {

    private static boolean initialized;
    private static ConnectionFactory connectionFactory;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);
    @Autowired
    BackendInterface backendWebService;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            insertDataset("pendingMessagesListDataset.sql");
            initialized = true;
            connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        }
    }

    @Test
    public void testListPendingMessagesOk() throws Exception {

        javax.jms.Connection connection = connectionFactory.createConnection("domibus", "changeit");
        connection.start();
        pushQueueMessage("2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu", connection, WS_NOT_QUEUE);
        pushQueueMessage("78a1d578-0cc7-41fb-9f35-86a5b2769a14@domibus.eu", connection, WS_NOT_QUEUE);
        pushQueueMessage("2bbc05d8-b603-4742-a118-137898a81de3@domibus.eu", connection, WS_NOT_QUEUE);
        connection.close();

        String request = new String("<listPendingMessagesRequest></listPendingMessagesRequest>");
        ListPendingMessagesResponse response = backendWebService.listPendingMessages(request);

        // Verifies the response
        Assert.assertNotNull(response);
        Assert.assertFalse(response.getMessageID().isEmpty());

        String fakeXml = FileUtils.readFileToString(new File("src/test/resources/dataset/as4/PML.xml"));
        for (String messageId : response.getMessageID()) {
            System.out.println("Response Message ID [:" + messageId + "]");
            Assert.assertTrue(fakeXml.contains(messageId));
        }

    }

    @Test
    public void testListPendingMessagesNOk() throws Exception {

        String request = new String("<listPendingMessagesRequest>1</listPendingMessagesRequest>");
        ListPendingMessagesResponse response = backendWebService.listPendingMessages(request);

        // Verifies the response
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getMessageID().isEmpty());
    }

}
