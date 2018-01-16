package eu.domibus.plugin.webService;

import eu.domibus.plugin.webService.generated.SubmitMessageFault;
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
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSubmitMessageValid() throws SubmitMessageFault, InterruptedException, SQLException {

        super.testSubmitMessageValid();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled, no mime-type specified on payload
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSubmitMessageValidNoMimeType() throws SubmitMessageFault, InterruptedException, SQLException {

       super.testSubmitMessageValidNoMimeType();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled and invalid payload Href
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSubmitMessageInvalidPayloadHref() throws SubmitMessageFault, InterruptedException {

        super.testSubmitMessageInvalidPayloadHref();
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSubmitMessagePayloadHrefMismatch() throws SubmitMessageFault, InterruptedException {

       super.testSubmitMessagePayloadHrefMismatch();
    }
}
