package eu.domibus.plugin.webService;

import eu.domibus.plugin.webService.generated.SendMessageFault;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by draguio on 17/02/2016.
 */
@ContextConfiguration("classpath:pmode-dao.xml")
@Ignore
public class SubmitMessageWithPayloadProfilePModeDaoIT extends SubmitMessageWithPayloadProfileIT {

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
    public void testSubmitMessageValid() throws SendMessageFault, InterruptedException, SQLException {

        super.testSubmitMessageValid();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled, no mime-type specified on payload
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSubmitMessageValidNoMimeType() throws SendMessageFault, InterruptedException, SQLException {

       super.testSubmitMessageValidNoMimeType();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled and invalid payload Href
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SendMessageFault.class)
    public void testSubmitMessageInvalidPayloadHref() throws SendMessageFault, InterruptedException {

        super.testSubmitMessageInvalidPayloadHref();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SendMessageFault.class)
    public void testSubmitMessagePayloadHrefMismatch() throws SendMessageFault, InterruptedException {

       super.testSubmitMessagePayloadHrefMismatch();
    }
}
