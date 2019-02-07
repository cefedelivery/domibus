package eu.domibus.ext.delegate.services.message;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian-Ion TINCU
 */
public class MessageServiceDelegateTest {

    private MessageServiceDelegate messageServiceDelegate = new MessageServiceDelegate();

    @Test
    public void trimsAndStripsMessageIdWhenCleaningIt() {
        String messageId = " \n\t -Dom137--  \t\n ";

        String trimmedMessageId = messageServiceDelegate.cleanMessageIdentifier(messageId);

        Assert.assertEquals("Should have trimmed spaces at the end of the message identifier when cleaning it",
                "-Dom137--", trimmedMessageId);
    }
}