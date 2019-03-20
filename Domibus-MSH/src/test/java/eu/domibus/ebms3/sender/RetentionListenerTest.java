package eu.domibus.ebms3.sender;

import com.google.common.collect.Lists;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class RetentionListenerTest {

    @Tested
    private RetentionListener retentionListener;

    @Injectable
    private UserMessageDefaultService userMessageDefaultService;

    @Injectable
    private Message message;

    @Test
    public void deletesMessagesByTheirMessageIdentifiers() throws JMSException {
        // Given
        new Expectations() {{
           message.getStringProperty(MessageConstants.MESSAGE_ID); result = "messageId";
        }};

        // When
        retentionListener.onMessage(message);

        // Then
        new Verifications() {{
           userMessageDefaultService.deleteMessage("messageId");
        }};
    }
}