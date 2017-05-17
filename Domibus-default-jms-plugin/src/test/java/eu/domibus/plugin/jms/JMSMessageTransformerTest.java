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
import java.util.Locale;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;
import static org.junit.Assert.*;

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
        Submission submission = createSubmission();
        final MapMessage mapMessage = jmsMessageTransformer.transformFromSubmission(submission, new ActiveMQMapMessage());
        validatePropertyNames(mapMessage);
        assertSubmission(submission, mapMessage);
    }

    @Test
    public void testDoubleTransformFromSubmission() throws Exception {
        Submission submission = createSubmission();
        final MapMessage mapMessage = jmsMessageTransformer.transformFromSubmission(submission, new ActiveMQMapMessage());
        final Submission transformedSubmission = jmsMessageTransformer.transformToSubmission(mapMessage);

        assertEquals(submission.getFromParties(), transformedSubmission.getFromParties());
        assertEquals(submission.getToParties(), transformedSubmission.getToParties());
        assertEquals(submission.getPayloads(), transformedSubmission.getPayloads());
        assertEquals(submission.getMessageProperties(), transformedSubmission.getMessageProperties());
    }

    protected Submission createSubmission() {
        Submission submission = new Submission();
        submission.setService("myService");
        submission.setAction("myAction");
        submission.setServiceType("myServiceType");
        submission.setConversationId("myConversationId");
        submission.setMessageId("myMessageId");
        submission.addFromParty("fromPartyId", "fromPartyType");
        submission.addToParty("toPartyId", "toPartyType");
        submission.setFromRole("fromRole");
        submission.setToRole("toRole");
        submission.addMessageProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, "C1");
        submission.addMessageProperty(JMSMessageConstants.PROPERTY_ENDPOINT, "myEndpoint");
        submission.addMessageProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, "C4");
        submission.addMessageProperty("prop1", "value1", "type1");
        submission.addMessageProperty("prop2", "value2", "type2");
        submission.setAgreementRef("myAgreementRef");
        submission.setRefToMessageId("myRefToMessageId");

        final Collection<Submission.TypedProperty> payloadProperties1 = new ArrayList<>();
        payloadProperties1.add(new Submission.TypedProperty(JMSMessageConstants.MIME_TYPE, "text/xml"));
        submission.addPayload("payloadContentId1", new DataHandler(new ByteArrayDataSource("payload1".getBytes(), "text/xml")), payloadProperties1, false, new Submission.Description(new Locale("en"), "payload1"), null);
        final Collection<Submission.TypedProperty> payloadProperties2 = new ArrayList<>();
        payloadProperties2.add(new Submission.TypedProperty(JMSMessageConstants.MIME_TYPE, "application/octet-stream"));
        submission.addPayload("payloadContentId2", new DataHandler(new ByteArrayDataSource("payload2".getBytes(), "application/octet-stream")), payloadProperties2, false, new Submission.Description(new Locale("en"), "payload3"), null);
        final Collection<Submission.TypedProperty> payloadProperties3 = new ArrayList<>();
        payloadProperties3.add(new Submission.TypedProperty(JMSMessageConstants.MIME_TYPE, "application/json"));
        submission.addPayload("payloadContentId3", new DataHandler(new ByteArrayDataSource("payload3".getBytes(), "application/json")), payloadProperties3, false, new Submission.Description(new Locale("en"), "payload3"), null);
        return submission;
    }

    protected void assertSubmission(Submission submission, MapMessage mapMessage) throws JMSException {
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.SERVICE), submission.getService());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.ACTION), submission.getAction());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.SERVICE_TYPE), submission.getServiceType());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.CONVERSATION_ID), submission.getConversationId());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.MESSAGE_ID), submission.getMessageId());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.MESSAGE_ID), submission.getMessageId());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.FROM_ROLE), submission.getFromRole());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.TO_ROLE), submission.getToRole());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.FROM_PARTY_ID), submission.getFromParties().iterator().next().getPartyId());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.FROM_PARTY_TYPE), submission.getFromParties().iterator().next().getPartyIdType());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.TO_PARTY_ID), submission.getToParties().iterator().next().getPartyId());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.TO_PARTY_TYPE), submission.getToParties().iterator().next().getPartyIdType());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.AGREEMENT_REF), submission.getAgreementRef());
        assertEquals(mapMessage.getStringProperty(JMSMessageConstants.REF_TO_MESSAGE_ID), submission.getRefToMessageId());

        for (final Submission.TypedProperty p : submission.getMessageProperties()) {
            if (p.getKey().equals(PROPERTY_ORIGINAL_SENDER)) {
                assertEquals(mapMessage.getStringProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER), p.getValue());
                continue;
            }
            if (p.getKey().equals(PROPERTY_ENDPOINT)) {
                assertEquals(mapMessage.getStringProperty(JMSMessageConstants.PROPERTY_ENDPOINT), p.getValue());
                continue;
            }
            if (p.getKey().equals(PROPERTY_FINAL_RECIPIENT)) {
                assertEquals(mapMessage.getStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT), p.getValue());
                continue;
            }
            assertEquals(mapMessage.getStringProperty(PROPERTY_PREFIX + p.getKey()), p.getValue());
            assertEquals(mapMessage.getStringProperty(PROPERTY_TYPE_PREFIX + p.getKey()), p.getType());
        }
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
