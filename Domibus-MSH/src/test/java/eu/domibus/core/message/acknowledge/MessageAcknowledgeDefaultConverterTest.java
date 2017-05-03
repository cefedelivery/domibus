package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageAcknowledgeDefaultConverterTest {

    @Tested
    MessageAcknowledgeDefaultConverter messageAcknowledgeDefaultConverter;

    @Test
    public void testCreate() throws Exception {
        String user = "baciuco";
        String messageId = "1";
        Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        String from = "C3";
        String to = "C4";
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "val1");
        properties.put("key2", "val2");

        final MessageAcknowledgementEntity messageAcknowledgementEntity = messageAcknowledgeDefaultConverter.create(user, messageId, acknowledgeTimestamp, from, to, properties);
        assertEquals(messageAcknowledgementEntity.getCreateUser(), user);
        assertEquals(messageAcknowledgementEntity.getMessageId(), messageId);
        assertEquals(messageAcknowledgementEntity.getAcknowledgeDate(), acknowledgeTimestamp);
        assertEquals(messageAcknowledgementEntity.getFrom(), from);
        assertEquals(messageAcknowledgementEntity.getTo(), to);
        assertEquals(messageAcknowledgementEntity.getPropertiesAsMap(), properties);
    }

    @Test
    public void testConvert() throws Exception {
        String user = "baciuco";
        String messageId = "1";
        Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        String from = "C3";
        String to = "C4";
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "val1");
        properties.put("key2", "val2");

        final MessageAcknowledgementEntity messageAcknowledgementEntity = messageAcknowledgeDefaultConverter.create(user, messageId, acknowledgeTimestamp, from, to, properties);

        final MessageAcknowledgement converted = messageAcknowledgeDefaultConverter.convert(messageAcknowledgementEntity);
        assertEquals(messageAcknowledgementEntity.getCreateUser(), converted.getCreateUser());
        assertEquals(messageAcknowledgementEntity.getMessageId(), converted.getMessageId());
        assertEquals(messageAcknowledgementEntity.getAcknowledgeDate(), converted.getAcknowledgeDate());
        assertEquals(messageAcknowledgementEntity.getFrom(), converted.getFrom());
        assertEquals(messageAcknowledgementEntity.getTo(), converted.getTo());
        assertEquals(messageAcknowledgementEntity.getPropertiesAsMap(), converted.getProperties());
    }
}
