package eu.domibus.plugin.jms;

import eu.domibus.plugin.Submission;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Assert;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.jms.MapMessage;
import javax.mail.util.ByteArrayDataSource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;

/**
 * Created by Arun Raj on 18/10/2016.
 */
public class JMSMessageTransformerTest
{

    private static final String DEFAULT_PROPERTIES_PATH = "./src/main/resources/business-defaults.properties";

    private static final String MIME_TYPE               = "MimeType";
    private static final String DEFAULT_MT              = "text/xml";
    private static final String DOMIBUS_BLUE            = "domibus-blue";
    private static final String DOMIBUS_RED             = "domibus-red";
    private static final String INITIATOR_ROLE          = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private static final String RESPONDER_ROLE          = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    private static final String PAYLOAD_ID              = "cid:message";
    private static final String UNREGISTERED_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String ORIGINAL_SENDER         = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
    private static final String FINAL_RECIPIENT         = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
    private static final String ACTION_TC1LEG1          = "TC1Leg1";
    private static final String PROTOCOL_AS4            = "AS4";
    private static final String SERVICE_NOPROCESS       = "bdx:noprocess";
    private static final String SERVICE_TYPE_TC1        = "tc1";

    /**
     * Testing basic happy flow scenario of the transform from submission of JMS transformer
     */
    @Test public void transformFromSubmission_HappyFlow() throws Exception
    {
	Submission submissionObj = new Submission();
	submissionObj.setAction(ACTION_TC1LEG1);
	submissionObj.setService(SERVICE_NOPROCESS);
	submissionObj.setServiceType(SERVICE_TYPE_TC1);
	submissionObj.setConversationId("123");
	submissionObj.setMessageId("1234");
	submissionObj.addFromParty(DOMIBUS_BLUE, UNREGISTERED_PARTY_TYPE);
	submissionObj.setFromRole(INITIATOR_ROLE);
	submissionObj.addToParty(DOMIBUS_RED, UNREGISTERED_PARTY_TYPE);
	submissionObj.setToRole(RESPONDER_ROLE);
	submissionObj.addMessageProperty(PROPERTY_ORIGINAL_SENDER, ORIGINAL_SENDER);
	submissionObj.addMessageProperty(PROPERTY_ENDPOINT, "http://localhost:8080/domibus/domibus-blue");
	submissionObj.addMessageProperty(PROPERTY_FINAL_RECIPIENT, FINAL_RECIPIENT);
	submissionObj.setAgreementRef("12345");
	submissionObj.setRefToMessageId("123456");

	String strPayLoad1 = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
	DataHandler payLoadDataHandler = new DataHandler(new ByteArrayDataSource(strPayLoad1.getBytes(), DEFAULT_MT));
	Submission.TypedProperty objTypedProperty = new Submission.TypedProperty(MIME_TYPE, DEFAULT_MT);
	Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
	listTypedProperty.add(objTypedProperty);
	Submission.Payload objPayload1 = new Submission.Payload(PAYLOAD_ID, payLoadDataHandler, listTypedProperty, false, null, null);
	submissionObj.addPayload(objPayload1);

	JMSMessageTransformer testObj = new JMSMessageTransformer(DEFAULT_PROPERTIES_PATH);
	MapMessage messageMap = new ActiveMQMapMessage();
	messageMap = testObj.transformFromSubmission(submissionObj, messageMap);

	Assert.assertEquals(DOMIBUS_BLUE, messageMap.getStringProperty(FROM_PARTY_ID));
	//        System.out.println(messageMap);

    }

    /*
     * Testing basic happy flow scenario of the transform from messaging to submission of JMS transformer
     */
    @Test public void transformToSubmission_HappyFlow() throws Exception
    {
	MapMessage messageMap = new ActiveMQMapMessage();
	messageMap.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, "submitMessage");
	messageMap.setStringProperty(JMSMessageConstants.SERVICE, SERVICE_NOPROCESS);
	messageMap.setStringProperty(JMSMessageConstants.SERVICE_TYPE, SERVICE_TYPE_TC1);
	messageMap.setStringProperty(JMSMessageConstants.ACTION, ACTION_TC1LEG1);
	messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_ID, DOMIBUS_BLUE);
	messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_TYPE, UNREGISTERED_PARTY_TYPE);
	messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_ID, DOMIBUS_RED);
	messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_TYPE, UNREGISTERED_PARTY_TYPE);
	messageMap.setStringProperty(JMSMessageConstants.FROM_ROLE, INITIATOR_ROLE);
	messageMap.setStringProperty(JMSMessageConstants.TO_ROLE, RESPONDER_ROLE);
	messageMap.setStringProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, ORIGINAL_SENDER);
	messageMap.setStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, FINAL_RECIPIENT);
	messageMap.setStringProperty(JMSMessageConstants.PROTOCOL, PROTOCOL_AS4);

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

    /*
     * Testing for bug EDELIVERY-1371, trimming whitespaces in the transform from UserMessage to Submission of JMS transformer
     */
    @Test public void transformToSubmission_TrimWhiteSpaces() throws Exception
    {
	MapMessage messageMap = new ActiveMQMapMessage();
	messageMap.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, "submitMessage");
	messageMap.setStringProperty(JMSMessageConstants.SERVICE, "\t" + SERVICE_NOPROCESS + "   ");
	messageMap.setStringProperty(JMSMessageConstants.SERVICE_TYPE, "\t" + SERVICE_TYPE_TC1 + "    ");
	messageMap.setStringProperty(JMSMessageConstants.ACTION, "    " + ACTION_TC1LEG1 + "\t");
	messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_ID, '\t' + DOMIBUS_BLUE + "\t");
	messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_TYPE, "   " + UNREGISTERED_PARTY_TYPE + '\t');
	messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_ID, "\t" + DOMIBUS_RED + "\t");
	messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_TYPE, "   " + UNREGISTERED_PARTY_TYPE + "\t");
	messageMap.setStringProperty(JMSMessageConstants.FROM_ROLE, "    " + INITIATOR_ROLE + "\t");
	messageMap.setStringProperty(JMSMessageConstants.TO_ROLE, '\t' + RESPONDER_ROLE + "   ");
	messageMap.setStringProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, "\t" + ORIGINAL_SENDER + "    ");
	messageMap.setStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, "\t" + FINAL_RECIPIENT + "\t");
	messageMap.setStringProperty(JMSMessageConstants.PROTOCOL, "\t" + PROTOCOL_AS4 + "\t\t");

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
	Assert.assertEquals(RESPONDER_ROLE, objSubmission.getToRole());

	Assert.assertEquals(SERVICE_NOPROCESS, objSubmission.getService());
	Assert.assertEquals(SERVICE_TYPE_TC1, objSubmission.getServiceType());
	Assert.assertEquals(ACTION_TC1LEG1, objSubmission.getAction());

	//        for(Iterator<Submission.TypedProperty> itr = objSubmission.getMessageProperties().iterator(); itr.hasNext();)
	//        {
	//            Submission.TypedProperty prop = itr.next();
	//            Assert.assertEquals(MIME_TYPE, prop.getType());
	//            Assert.assertEquals(DEFAULT_MT, prop.getValue());
	//        }
    }

}