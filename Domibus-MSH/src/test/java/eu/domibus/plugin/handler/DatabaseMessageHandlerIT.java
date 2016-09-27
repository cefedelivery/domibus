package eu.domibus.plugin.handler;

import eu.domibus.AbstractIT;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

/**
 * @author Federico Martini
 * @since 3.2
 */
public class DatabaseMessageHandlerIT extends AbstractIT {

    private static boolean initialized;

    @Autowired
    private DatabaseMessageHandler dmh;

    @Before
    public void before() {
        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageDataset.sql");
            initialized = true;
        }
    }

    @Test
    public void testSubmitMessageOK() throws SendMessageFault, InterruptedException, SQLException {
        //TODO

    }

    @Test
    public void testDownloadMessageOK() throws SendMessageFault, InterruptedException, SQLException {
        //TODO

    }


}
