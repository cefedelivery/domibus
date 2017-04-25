package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.message.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.ext.delegate.converter.DomibusDomainConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
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
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageAcknowledgeServiceDelegateTest {

    @Tested
    MessageAcknowledgeServiceDelegate messageAcknowledgeServiceDelegate;

    @Injectable
    MessageAcknowledgeService messageAcknowledgeService;

    @Injectable
    DomibusDomainConverter domainConverter;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    SecurityService securityService;

    @Test
    public void testAcknowledgeMessageDelivered() throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");

        final MessageAcknowledgement messageAcknowledgement = new MessageAcknowledgement();

        new Expectations(messageAcknowledgeServiceDelegate) {{
            messageAcknowledgeService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
            result = messageAcknowledgement;

        }};

        messageAcknowledgeServiceDelegate.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);

        new Verifications() {{
            messageAcknowledgeService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
            domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
        }};
    }

    @Test
    public void testAcknowledgeMessageDeliveredWithNoProperties() throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());

        new Expectations(messageAcknowledgeServiceDelegate) {{
            messageAcknowledgeService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
        }};

        messageAcknowledgeServiceDelegate.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp);
    }

    @Test
    public void testGetAcknowledgeMessages() throws Exception {
        final String messageId = "1";
        final List<MessageAcknowledgement> messageAcknowledgements = new ArrayList<>();
        messageAcknowledgements.add(new MessageAcknowledgement());
        messageAcknowledgements.add(new MessageAcknowledgement());


        new Expectations(messageAcknowledgeServiceDelegate) {{
            messageAcknowledgeService.getAcknowledgedMessages(messageId);
            result = messageAcknowledgements;

        }};

        messageAcknowledgeServiceDelegate.getAcknowledgedMessages(messageId);

        new Verifications() {{
            domainConverter.convert(messageAcknowledgements, MessageAcknowledgementDTO.class);
        }};
    }
}
