package eu.domibus.plugin.jms;

import eu.domibus.plugin.Submission;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.MapMessage;

import java.text.MessageFormat;
import java.util.Iterator;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;
import static org.springframework.util.StringUtils.trimWhitespace;

/**
 * Created by venugar on 18/10/2016.
 */
public class JMSMessageTransformerTest
{

    private static final String DEFAULT_PROPERTIES_PATH = "./src/main/resources/business-defaults.properties";

    private static final String MIME_TYPE               = "MimeType";
    private static final String DEFAULT_MT              = "text/xml";
    private static final String DOMIBUS_BLUE            = "domibus-blue";
    private static final String DOMIBUS_RED             = "domibus-red";
    private static final String INITIATOR_ROLE          = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private static final String RESPONDEER_ROLE         = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    private static final String PAYLOAD_ID              = "cid:message";
    private static final String UNREGISTERED_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";


    @Test public void transformFromSubmission() throws Exception
    {

    }

    @Test public void transformToSubmission_HappyFlow() throws Exception
    {
        MapMessage messageMap = new ActiveMQMapMessage();
        messageMap.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, "submitMessage");
        messageMap.setStringProperty(JMSMessageConstants.SERVICE, "bdx:noprocess");
        messageMap.setStringProperty(JMSMessageConstants.SERVICE_TYPE, "tc1");
        messageMap.setStringProperty(JMSMessageConstants.ACTION, "TC1Leg1");
        messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_ID, DOMIBUS_BLUE);
        messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_TYPE, UNREGISTERED_PARTY_TYPE);
        messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_ID, DOMIBUS_RED);
        messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_TYPE, UNREGISTERED_PARTY_TYPE);
        messageMap.setStringProperty(JMSMessageConstants.FROM_ROLE, INITIATOR_ROLE);
        messageMap.setStringProperty(JMSMessageConstants.TO_ROLE, RESPONDEER_ROLE);
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        messageMap.setStringProperty(JMSMessageConstants.PROTOCOL, "AS4");

        // Optional
        // messageMap.setStringProperty("conversationId", "123");
        // messageMap.setStringProperty("refToMessageId", "11");
        // messageMap.setStringProperty("messageId", "12345");

        messageMap.setJMSCorrelationID("12345");

        messageMap.setStringProperty(JMSMessageConstants.TOTAL_NUMBER_OF_PAYLOADS, "1");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, 1), PAYLOAD_ID);
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, 1), DEFAULT_MT);
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_DESCRIPTION_FORMAT, 1), "message");
        String pay1 = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        byte[] payload = pay1.getBytes();
        messageMap.setBytes(MessageFormat.format(PAYLOAD_NAME_FORMAT, 1), payload);


        JMSMessageTransformer testObj = new JMSMessageTransformer(DEFAULT_PROPERTIES_PATH);
        Submission objSubmission = testObj.transformToSubmission(messageMap);
        Assert.assertNotNull(objSubmission);
    }



    @Test public void transformToSubmission_TrimWhiteSpaces() throws Exception
    {
        MapMessage messageMap = new ActiveMQMapMessage();
        messageMap.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, "submitMessage");
        messageMap.setStringProperty(JMSMessageConstants.SERVICE, "\tbdx:noprocess   ");
        messageMap.setStringProperty(JMSMessageConstants.SERVICE_TYPE, "\ttc1    ");
        messageMap.setStringProperty(JMSMessageConstants.ACTION, "    TC1Leg1\t");
        messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_ID, '\t'+DOMIBUS_BLUE + "\t");
        messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_TYPE, "   " + UNREGISTERED_PARTY_TYPE + '\t');
        messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_ID, "\t" + DOMIBUS_RED + "\t");
        messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_TYPE, "   " + UNREGISTERED_PARTY_TYPE + "\t");
        messageMap.setStringProperty(JMSMessageConstants.FROM_ROLE, "    " + INITIATOR_ROLE + "\t");
        messageMap.setStringProperty(JMSMessageConstants.TO_ROLE, '\t' + RESPONDEER_ROLE + "   ");
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, "\turn:oasis:names:tc:ebcore:partyid-type:unregistered:C1    ");
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, "\turn:oasis:names:tc:ebcore:partyid-type:unregistered:C4\t");
        messageMap.setStringProperty(JMSMessageConstants.PROTOCOL, "\tAS4\t\t");

        // Optional
        // messageMap.setStringProperty("conversationId", "123");
        // messageMap.setStringProperty("refToMessageId", "11");
        // messageMap.setStringProperty("messageId", "12345");

        messageMap.setJMSCorrelationID("12345");

        messageMap.setStringProperty(JMSMessageConstants.TOTAL_NUMBER_OF_PAYLOADS, "1");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, 1), "\t" + PAYLOAD_ID + "   ");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, 1), "   " + DEFAULT_MT + "\t\t");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_DESCRIPTION_FORMAT, 1), "\t\tmessage    ");
        String pay1 = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        byte[] payload = pay1.getBytes();
        messageMap.setBytes(MessageFormat.format(PAYLOAD_NAME_FORMAT, 1), payload);


        JMSMessageTransformer testObj = new JMSMessageTransformer(DEFAULT_PROPERTIES_PATH);
        Submission objSubmission = testObj.transformToSubmission(messageMap);
        Assert.assertNotNull("Submission object in the response should not be null:", objSubmission);
        for (Iterator<Submission.Party> itr = objSubmission.getFromParties().iterator(); itr.hasNext(); )
        {
            Submission.Party fromPartyObj = itr.next();
            Assert.assertEquals(DOMIBUS_BLUE, fromPartyObj.getPartyId());
            Assert.assertEquals(UNREGISTERED_PARTY_TYPE, fromPartyObj.getPartyIdType());
        }
        Assert.assertEquals(INITIATOR_ROLE, objSubmission.getFromRole());

        for (Iterator<Submission.Party> itr = objSubmission.getToParties().iterator(); itr.hasNext(); )
        {
            Submission.Party toPartyObj = itr.next();
            Assert.assertEquals(DOMIBUS_RED, toPartyObj.getPartyId());
            Assert.assertEquals(UNREGISTERED_PARTY_TYPE, toPartyObj.getPartyIdType());
        }
        Assert.assertEquals(RESPONDEER_ROLE, objSubmission.getToRole());

        Assert.assertEquals("bdx:noprocess", objSubmission.getService());
        Assert.assertEquals("tc1", objSubmission.getServiceType());
        Assert.assertEquals("TC1Leg1", objSubmission.getAction());

        for(Iterator<Submission.TypedProperty> itr = objSubmission.getMessageProperties().iterator(); itr.hasNext();)
        {
            Submission.TypedProperty prop = itr.next();
//            Assert.assertEquals(MIME_TYPE, prop.getType());
//            Assert.assertEquals(DEFAULT_MT, prop.getValue());
        }
    }


    @Test
    public void test()
    {
        String s1 = null;
        String s2 = "  wr  ";
        System.out.println(trimWhitespace(s2));
    }
}