package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.ebms3.UserMessageService;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.ebms3.common.UserMessageServiceHelper;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageAcknowledgeDefaultServiceTest {

    @Tested
    MessageAcknowledgeDefaultService messageAcknowledgeDefaultService;

    @Injectable
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    UserMessageServiceHelper userMessageServiceHelper;

    @Test
    public void testAcknowledgeMessageDelivered(@Injectable  final MessageAcknowledgementEntity entity) throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final String finalRecipient = "C4";
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");

        final UserMessage userMessage = new UserMessage();
        final String localAccessPointId ="C3";

        new Expectations(messageAcknowledgeDefaultService) {{
            messageAcknowledgeDefaultService.getUserMessage(messageId);
            result = userMessage;

            messageAcknowledgeDefaultService.getLocalAccessPointId(userMessage);
            result = localAccessPointId;

            userMessageServiceHelper.getFinalRecipient(userMessage);
            result = finalRecipient;

            messageAcknowledgeDefaultService.acknowledgeMessage(userMessage, acknowledgeTimestamp, localAccessPointId, finalRecipient, properties);

        }};

        messageAcknowledgeDefaultService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
    }


    @Test
    public void testAcknowledgeMessageDeliveredWithNoProperties(@Injectable  final MessageAcknowledgementEntity entity) throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());

        new Expectations(messageAcknowledgeDefaultService) {{
            messageAcknowledgeDefaultService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);

        }};

        messageAcknowledgeDefaultService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp);

        new Verifications() {{
            messageAcknowledgeDefaultService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp,null);
        }};
    }

    @Test
    public void testAcknowledgeMessageProcessed(@Injectable  final MessageAcknowledgementEntity entity) throws Exception {
        final UserMessage userMessage = new UserMessage();
        final String localAccessPointId ="C3";

        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final String finalRecipient = "C4";
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");

        new Expectations(messageAcknowledgeDefaultService) {{
            messageAcknowledgeDefaultService.getUserMessage(messageId);
            result = userMessage;

            messageAcknowledgeDefaultService.getLocalAccessPointId(userMessage);
            result = localAccessPointId;

            userMessageServiceHelper.getFinalRecipient(userMessage);
            result = finalRecipient;

            messageAcknowledgeDefaultService.acknowledgeMessage(userMessage, acknowledgeTimestamp, finalRecipient, localAccessPointId, properties);

        }};

        messageAcknowledgeDefaultService.acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, properties);
    }

    @Test
    public void testGetAcknowledgedMessages() throws Exception {
        final String messageId = "1";
        final List<MessageAcknowledgementEntity> messageAcknowledgements = new ArrayList<>();
        messageAcknowledgements.add(new MessageAcknowledgementEntity());
        messageAcknowledgements.add(new MessageAcknowledgementEntity());


        new Expectations(messageAcknowledgeDefaultService) {{
            messageAcknowledgementDao.findByMessageId(messageId);
            result = messageAcknowledgements;

        }};

        messageAcknowledgeDefaultService.getAcknowledgedMessages(messageId);

        new Verifications() {{
            messageAcknowledgeConverter.convert(messageAcknowledgements);
        }};
    }

    @Test
    public void testAcknowledgeMessage(@Injectable final UserMessage userMessage,
                                       @Injectable final MessageAcknowledgementEntity entity) throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final String from = "C3";
        final String to = "C4";
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");
        final String user = "baciuco";


        new Expectations(messageAcknowledgeDefaultService) {{
            authUtils.getAuthenticatedUser();
            result = user;

            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            messageAcknowledgeConverter.create(user, messageId, acknowledgeTimestamp, from, to, properties);
            result = entity;

        }};

        messageAcknowledgeDefaultService.acknowledgeMessage(userMessage, acknowledgeTimestamp, from, to, properties);

        new Verifications() {{
            messageAcknowledgementDao.create(entity);
            messageAcknowledgeConverter.convert(entity);
        }};
    }
}
