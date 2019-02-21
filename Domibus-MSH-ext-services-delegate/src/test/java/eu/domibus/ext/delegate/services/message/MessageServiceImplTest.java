package eu.domibus.ext.delegate.services.message;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian-Ion TINCU
 */
public class MessageServiceImplTest {

    private MessageServiceImpl messageService = new MessageServiceImpl();

    @Test
    public void trimsAndStripsMessageIdWhenCleaningIt() {
        String messageId = " \n\t -Dom137--  \t\n ";

        String trimmedMessageId = messageService.cleanMessageIdentifier(messageId);

        Assert.assertEquals("Should have trimmed spaces at the end of the message identifier when cleaning it",
                "-Dom137--", trimmedMessageId);
    }

    @Test
    public void doesNothingToAMessageIdWithNeitherWhitespaceNorControlCharactersWhenCleaningIt() {
        String messageId = "-Dom138--";

        String trimmedMessageId = messageService.cleanMessageIdentifier(messageId);

        Assert.assertEquals("Should have returned the message as is when cleaning it if the message does not container whitespace nor control characters",
                "-Dom138--", trimmedMessageId);
    }
}