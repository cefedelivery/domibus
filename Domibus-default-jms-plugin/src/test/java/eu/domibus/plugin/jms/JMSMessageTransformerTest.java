package eu.domibus.plugin.jms;

import eu.domibus.plugin.Submission;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.mail.util.ByteArrayDataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class JMSMessageTransformerTest {

    private static final Log LOG = LogFactory.getLog(JMSMessageTransformerTest.class);

    @Tested
    JMSMessageTransformer jmsMessageTransformer;

    @Test
    public void testTransformFromSubmission() throws Exception {
        Submission submission = new Submission();
        submission.setService("myService");
        submission.setAction("myAction");
        submission.setServiceType("myServiceType");
        submission.setConversationId("myConversationId");
        submission.setMessageId("myMessageId");
        submission.addFromParty("fromPartyId", "fromPartyType");
        submission.addFromParty("toPartyId", "toPartyType");
        submission.setFromRole("fromRole");
        submission.setToRole("toRole");
        submission.addMessageProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, "C1");
        submission.addMessageProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, "C4");
        submission.addMessageProperty(JMSMessageConstants.PROPERTY_ENDPOINT, "myEndpoint");
        submission.addMessageProperty("prop1", "value1", "type1");
        submission.addMessageProperty("prop2", "value2", "type2");
        submission.setAgreementRef("myAgreementRef");
        submission.setRefToMessageId("myRefToMessageId");

        final Collection<Submission.TypedProperty> bodyPayloadProperties = new ArrayList<>();
        Submission.TypedProperty bodyPayloadMimeProperty = new Submission.TypedProperty(JMSMessageConstants.MIME_TYPE, "text/xml");
        bodyPayloadProperties.add(bodyPayloadMimeProperty);
        submission.addPayload("bodyContentId", new DataHandler(new ByteArrayDataSource("payloadBody".getBytes(), "text/xml")), bodyPayloadProperties, true, null, null);
        final Collection<Submission.TypedProperty> payloadProperties = new ArrayList<>();
        Submission.TypedProperty payloadMimeProperty = new Submission.TypedProperty(JMSMessageConstants.MIME_TYPE, "application/octet-stream");
        payloadProperties.add(payloadMimeProperty);
        submission.addPayload("payload1", new DataHandler(new ByteArrayDataSource("payloadBody".getBytes(), "application/octet-stream")), payloadProperties, true, null, null);


        MapMessage messageOut = new ActiveMQMapMessage();
        final MapMessage mapMessage = jmsMessageTransformer.transformFromSubmission(submission, messageOut);
        validatePropertyNames(mapMessage);
    }


    protected void validatePropertyNames(MapMessage mapMessage) throws JMSException {
        final Enumeration propertyNames = mapMessage.getPropertyNames();
        assertNotNull(propertyNames);
        while (propertyNames.hasMoreElements()) {
            Object propertyName = propertyNames.nextElement();

            if (!(propertyName instanceof String)) {
                fail("Property [" + propertyName + "] should be of type String");
            }

            if (!isJavaIdentifier((String) propertyName)) {
                fail("Property [" + propertyName + "] is not a valid java identifier");
            }
        }
    }


    protected boolean isJavaIdentifier(String value) {
        LOG.info("Checking if [" + value + "] is a java identifier");
        final char[] chars = value.toCharArray();
        for (char aChar : chars) {
            if (!Character.isJavaIdentifierPart(aChar)) {
                LOG.info("Character [" + aChar + "] is not a java identifier part");
                return false;
            }
        }
        return true;
    }
}
