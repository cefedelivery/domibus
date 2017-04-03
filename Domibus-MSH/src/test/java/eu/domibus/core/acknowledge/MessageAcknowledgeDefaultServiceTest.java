package eu.domibus.core.acknowledge;

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

    @Test
    public void testAcknowledgeMessage(@Injectable  final MessageAcknowledgementEntity entity) throws Exception {
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

            messageAcknowledgeConverter.create(user, messageId, acknowledgeTimestamp, from, to, properties);
            result = entity;

        }};

        messageAcknowledgeDefaultService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, properties);

        new Verifications() {{
            messageAcknowledgementDao.create(entity);
            messageAcknowledgeConverter.convert(entity);
        }};
    }

    @Test
    public void testAcknowledgeMessageWithNoProperties(@Injectable  final MessageAcknowledgementEntity entity) throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final String from = "C3";
        final String to = "C4";

        new Expectations(messageAcknowledgeDefaultService) {{
            messageAcknowledgeDefaultService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, null);

        }};

        messageAcknowledgeDefaultService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to);

        new Verifications() {{
            messageAcknowledgeDefaultService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, null);
        }};
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
}
