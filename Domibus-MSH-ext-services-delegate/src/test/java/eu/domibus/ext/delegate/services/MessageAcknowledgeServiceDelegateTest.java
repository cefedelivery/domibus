package eu.domibus.ext.delegate.services;

import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.delegate.converter.DomibusDomainConverter;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
    AuthUtils authUtils;

    @Test
    public void testAcknowledgeMessage() throws Exception {
       /* final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final String from = "C3";
        final String to = "C4";
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");

        final MessageAcknowledgement messageAcknowledgement = new MessageAcknowledgement();

        new Expectations(messageAcknowledgeServiceDelegate) {{
            //don't execute method
            messageAcknowledgeServiceDelegate.checkSecurity();

            messageAcknowledgeService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, properties);
            result = messageAcknowledgement;

        }};

        messageAcknowledgeServiceDelegate.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, properties);

        new Verifications() {{
            messageAcknowledgeService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, properties);
            domainConverter.convert(messageAcknowledgement);
        }};*/
    }

   /* @Test
    public void testAcknowledgeMessageWithNoProperties() throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final String from = "C3";
        final String to = "C4";

        new Expectations(messageAcknowledgeServiceDelegate) {{
            //don't execute method
            messageAcknowledgeServiceDelegate.checkSecurity();

            messageAcknowledgeService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, null);
        }};

        messageAcknowledgeServiceDelegate.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to );
    }

    @Test(expected = MessageAcknowledgeException.class)
    public void testAcknowledgeMessageWhenExceptionIsRaised() throws Exception {
        final String messageId = "1";
        final Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        final String from = "C3";
        final String to = "C4";
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");

        new Expectations(messageAcknowledgeServiceDelegate) {{
            //don't execute method
            messageAcknowledgeServiceDelegate.checkSecurity();

            messageAcknowledgeService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, properties);
            result = new eu.domibus.api.acknowledge.MessageAcknowledgeException(DomibusCoreErrorCode.DOM_001, "raised exception");
        }};

        messageAcknowledgeServiceDelegate.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, properties);
    }

    @Test(expected = AuthenticationException.class)
    public void testCheckSecurity() throws Exception {
        new Expectations(messageAcknowledgeServiceDelegate) {{
            //don't execute method
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.hasUserOrAdminRole();
            result = new AccessDeniedException("access denied");
        }};

        messageAcknowledgeServiceDelegate.checkSecurity();
    }

    @Test
    public void testGetAcknowledgeMessages() throws Exception {
        final String messageId = "1";
        final List<MessageAcknowledgement> messageAcknowledgements = new ArrayList<>();
        messageAcknowledgements.add(new MessageAcknowledgement());
        messageAcknowledgements.add(new MessageAcknowledgement());


        new Expectations(messageAcknowledgeServiceDelegate) {{
            //don't execute method
            messageAcknowledgeServiceDelegate.checkSecurity();

            messageAcknowledgeService.getAcknowledgedMessages(messageId);
            result = messageAcknowledgements;

        }};

        messageAcknowledgeServiceDelegate.getAcknowledgedMessages(messageId);

        new Verifications() {{
            domainConverter.convert(messageAcknowledgements);
        }};
    }*/
}
