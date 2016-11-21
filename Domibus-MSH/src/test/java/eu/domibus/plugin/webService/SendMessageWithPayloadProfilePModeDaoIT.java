package eu.domibus.plugin.webService;

import eu.domibus.plugin.webService.generated.SendMessageFault;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by draguio on 17/02/2016.
 */
@ContextConfiguration("classpath:pmode-dao.xml")
public class SendMessageWithPayloadProfilePModeDaoIT extends SendMessageWithPayloadProfileIT {

    private static boolean initialized;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageWithPayloadProfileDataset.sql");
            initialized = true;
        }
    }


    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageValid() throws SendMessageFault, InterruptedException, SQLException {

        super.testSendMessageValid();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled, no mime-type specified on payload
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageValidNoMimeType() throws SendMessageFault, InterruptedException, SQLException {

       super.testSendMessageValidNoMimeType();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled and invalid payload Href
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SendMessageFault.class)
    public void testSendMessageInvalidPayloadHref() throws SendMessageFault, InterruptedException {

        super.testSendMessageInvalidPayloadHref();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SendMessageFault.class)
    public void testSendMessagePayloadHrefMismatch() throws SendMessageFault, InterruptedException {

       super.testSendMessagePayloadHrefMismatch();
    }
}
