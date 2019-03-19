package eu.domibus.ebms3.sender;

import com.google.common.collect.Lists;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Message;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class RetentionListenerTest {

    @Tested
    private RetentionListener retentionListener;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private UserMessageLogService userMessageLogService;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private Message message;

    @Test
    public void marksTheUserMessageAsDeleted() throws Exception {
        final String messageId = "1";

        new Expectations(retentionListener) {{
            retentionListener.handleSignalMessageDelete(messageId);
            message.getStringProperty(MessageConstants.MESSAGE_ID); result = messageId;
        }};

        retentionListener.onMessage(message);

        new Verifications() {{
            messagingDao.clearPayloadData(messageId);
            userMessageLogService.setMessageAsDeleted(messageId);
        }};
    }

    @Test
    public void clearsSignalMessagesReferencingTheMessageIdOfTheMessageBeingDeleted(@Injectable SignalMessage signalMessage) {
        final String messageId = "1";

        new Expectations() {{
            signalMessageDao.findSignalMessagesByRefMessageId(messageId); result = Lists.newArrayList(signalMessage);
        }};

        retentionListener.handleSignalMessageDelete(messageId);

        new Verifications() {{
            signalMessageDao.clear(signalMessage);
        }};
    }

    @Test
    public void doesNotClearAnySignalMessagesWhenNoSignalMessagesFoundReferencingTheMessageIdOfTheMessageBeingDeleted() throws Exception {
        final String messageId = "1";

        new Expectations() {{
            signalMessageDao.findSignalMessagesByRefMessageId(messageId); result = Lists.<SignalMessage>newArrayList();
        }};

        retentionListener.handleSignalMessageDelete(messageId);

        new Verifications() {{
            signalMessageDao.clear((SignalMessage) any); times = 0;
        }};
    }

    @Test
    public void marksSignalMessagesAsDeletedWhenReferencingTheMessageIdOfTheMessageBeingDeleted() {
        final String messageId = "1";
        final String signalMessageId = "signalMessageId";

        new Expectations() {{
            signalMessageDao.findSignalMessageIdsByRefMessageId(messageId); result = Lists.newArrayList(signalMessageId);
        }};

        retentionListener.handleSignalMessageDelete(messageId);

        new Verifications() {{
            userMessageLogService.setMessageAsDeleted(signalMessageId);
        }};
    }

    @Test
    public void doesNotMarkAnySignalMessagesAsDeletedWhenNoSignalMessagesIdentifiersFoundReferencingTheMessageIdOfTheMessageBeingDeleted() throws Exception {
        final String messageId = "1";

        new Expectations() {{
            signalMessageDao.findSignalMessageIdsByRefMessageId(messageId); result = Lists.<String>newArrayList();
        }};

        retentionListener.handleSignalMessageDelete(messageId);

        new Verifications() {{
            userMessageLogService.setMessageAsDeleted(anyString); times = 0;
        }};
    }
}